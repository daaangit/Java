import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Dispatcher implements Runnable
{

    private final List<Elevator> elevators;
    private final BlockingQueue<Request> incoming;

    public Dispatcher(List<Elevator> elevators, BlockingQueue<Request> incoming)
    {
        this.elevators = elevators;
        this.incoming = incoming;
    }

    @Override
    public void run()
    {
        Log.info("Dispatcher", "Started");
        while (!Thread.currentThread().isInterrupted())
            {
            try
            {
                Request r = incoming.take();
                Log.info("Dispatcher", "Received " + r);

                Elevator e = selectElevator(r);
                Log.info("Dispatcher", "Assigned " + r + " -> Elevator-" + e.getId());
                e.assign(r);
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt();
            }
            catch (Exception ex)
            {
                Log.info("Dispatcher", "ERROR: " + ex.getMessage());
            }
        }
        Log.info("Dispatcher", "Stopped");
    }

    private Elevator selectElevator(Request r)
    {
        Elevator best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Elevator e : elevators)
        {
            int score = scoreElevator(e, r);
            if (best == null || score > bestScore)
            {
                best = e;
                bestScore = score;
            }
        }

        if (best == null)
            best = elevators.get(0);
        return best;
    }

    private int scoreElevator(Elevator e, Request r) {
        int eFloor = e.getFloor();
        Direction eDir = e.getDirection();
        ElevatorStatus st = e.getStatus();

        int dist = Math.abs(eFloor - r.getFromFloor());
        int score = 1000 - dist * 10;

        if (st == ElevatorStatus.STOPPED && eDir == Direction.IDLE)
        {
            score += 200;
        }

        if (eDir == r.getDirection())
            {
            if (eDir == Direction.UP && eFloor <= r.getFromFloor()) score += 120;
            if (eDir == Direction.DOWN && eFloor >= r.getFromFloor()) score += 120;
        }

        if (!e.canAcceptOneMorePassenger())
        {
            score -= 500;
        }

        return score;
    }
}
