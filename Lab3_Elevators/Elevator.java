import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Elevator implements Runnable
{

    private final int id;
    private final int minFloor;
    private final int maxFloor;
    private final int capacity;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition hasStops = lock.newCondition();

    private int floor;
    private Direction direction = Direction.IDLE;
    private ElevatorStatus status = ElevatorStatus.STOPPED;

    private int passengers = 0;

    private final NavigableSet<Integer> stops = new ConcurrentSkipListSet<>();
    private final List<PassengerTask> tasks = new ArrayList<>();

    private final long moveStepMs;
    private final long doorsMs;

    public Elevator(int id, int minFloor, int maxFloor, int capacity, int startFloor,
                   long moveStepMs, long doorsMs)
    {
        this.id = id;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.capacity = capacity;
        this.floor = startFloor;
        this.moveStepMs = moveStepMs;
        this.doorsMs = doorsMs;
    }

    public int getId()
    {
        return id;
    }

    public int getFloor()
    {
        lock.lock();
        try
        {
            return floor;
        }
        finally
        {
            lock.unlock();
        }
    }

    public Direction getDirection()
    {
        lock.lock();
        try
        {
            return direction;
        }
        finally
        {
            lock.unlock();
        }
    }

    public ElevatorStatus getStatus()
    {
        lock.lock();
        try
        {
            return status;
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getPassengers()
    {
        lock.lock();
        try
        {
            return passengers;
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getCapacity()
    {
        return capacity;
    }

    public void assign(Request request)
    {
        lock.lock();
        try
        {
            tasks.add(new PassengerTask(request));
            stops.add(request.getFromFloor());
            stops.add(request.getToFloor());
            hasStops.signal(); // check aft
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean canAcceptOneMorePassenger()
    {
        lock.lock();
        try
        {
            return passengers < capacity;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public void run()
    {
        Log.info(tag(), "Started at floor " + floor);
        while (!Thread.currentThread().isInterrupted())
            {
            try
            {
                Integer next = awaitNextStop();
                if (next == null)
                    continue;
                moveTo(next);
                handleArrival();
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt();
            }
            catch (Exception ex)
            {
                Log.info(tag(), "ERROR: " + ex.getMessage());
            }
        }
        Log.info(tag(), "Stopped");
    }

    private String tag()
    {
        return "Elevator-" + id;
    }

    private Integer awaitNextStop() throws InterruptedException
    {
        lock.lock();
        try
        {
            while (stops.isEmpty())
            {
                direction = Direction.IDLE;
                status = ElevatorStatus.STOPPED;
                hasStops.await();
            }
            return chooseNextStop();
        }
        finally
        {
            lock.unlock();
        }
    }

    private Integer chooseNextStop()
    {
        if (stops.isEmpty())return null;

        if (direction == Direction.UP)
            {
            Integer up = stops.ceiling(floor);
            if (up != null) return up;
            direction = Direction.DOWN;
            return stops.floor(floor);
        }
        if (direction == Direction.DOWN)
            {
            Integer down = stops.floor(floor);
            if (down != null) return down;
            direction = Direction.UP;
            return stops.ceiling(floor);
        }

        Integer below = stops.floor(floor);
        Integer above = stops.ceiling(floor);
        if (below == null)
        {
            direction = Direction.UP;
            return above;
        }
        if (above == null)
        {
            direction = Direction.DOWN;
            return below;
        }
        int db = Math.abs(floor - below);
        int da = Math.abs(above - floor);
        if (da <= db)
        {
            direction = Direction.UP;
            return above;
        }
        else
        {
            direction = Direction.DOWN;
            return below;
        }
    }

    private void moveTo(int target) throws InterruptedException {
        lock.lock();
        try
        {
            closeDoorsLocked();
            status = ElevatorStatus.MOVING;
            if (target > floor) direction = Direction.UP;
            else if (target < floor) direction = Direction.DOWN;
            else direction = Direction.IDLE;
        }
        finally
        {
            lock.unlock();
        }

        while (true)
        {
            int cur;
            lock.lock();
            try {
                cur = floor;
                if (cur == target) {
                    status = ElevatorStatus.STOPPED;
                    break;
                }
                if (direction == Direction.UP) {
                    if (floor < maxFloor) floor++;
                } else if (direction == Direction.DOWN) {
                    if (floor > minFloor) floor--;
                }
                cur = floor;
            }
            finally
            {
                lock.unlock();
            }
            Log.info(tag(), "Passing floor " + cur);
            Thread.sleep(moveStepMs);
        }

        Log.info(tag(), "Arrived at floor " + target);
    }

    private void handleArrival() throws InterruptedException
    {
        lock.lock();
        try
        {
            stops.remove(floor);

            openDoorsLocked();
        }
        finally
        {
            lock.unlock();
        }

        Thread.sleep(doorsMs);

        lock.lock();
        try {
            int dropped = 0;
            int picked = 0;

            for (Iterator<PassengerTask> it = tasks.iterator(); it.hasNext(); )
                {
                PassengerTask t = it.next();
                if (t.pickedUp && t.request.getToFloor() == floor)
                    {
                    passengers = Math.max(0, passengers - 1);
                    dropped++;
                    it.remove();
                    Log.info(tag(), "Dropped off " + t.request);
                }
            }
            for (PassengerTask t : tasks) {
                if (!t.pickedUp && t.request.getFromFloor() == floor)
                    {
                    if (passengers < capacity)
                    {
                        passengers++;
                        t.pickedUp = true;
                        picked++;
                        Log.info(tag(), "Picked up " + t.request + " (passengers=" + passengers + "/" + capacity + ")");
                    }else 
                    {
                        stops.add(floor);
                        Log.info(tag(), "FULL at pickup for " + t.request + " (will retry)");
                    }
                }
            }

            if (dropped == 0 && picked == 0) {
                Log.info(tag(), "Stop with no passenger exchange");
            }

            closeDoorsLocked();

            if (stops.isEmpty())
                direction = Direction.IDLE;
        }
        finally
        {
            lock.unlock();
        }
    }

    private void openDoorsLocked()
    {
        status = ElevatorStatus.DOORS_OPEN;
        Log.info(tag(), "Doors OPEN on floor " + floor);
    }

    private void closeDoorsLocked()
    {
        if (status == ElevatorStatus.DOORS_OPEN)
            {
            Log.info(tag(), "Doors CLOSE on floor " + floor);
        }
        status = ElevatorStatus.STOPPED;
    }
}
