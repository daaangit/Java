import java.util.concurrent.atomic.AtomicLong;

/**
 * A passenger request: call elevator at fromFloor with direction, then go to toFloor.
 */
public final class Request 
{
    private static final AtomicLong SEQ = new AtomicLong(1);

    private final long id;
    private final int fromFloor;
    private final int toFloor;
    private final Direction direction;

    public Request(int fromFloor, int toFloor)
    {
        if (fromFloor == toFloor)
            throw new IllegalArgumentException("fromFloor and toFloor must differ");
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.direction = (toFloor > fromFloor) ? Direction.UP : Direction.DOWN;
        this.id = SEQ.getAndIncrement();
    }

    public long getId()
    {
        return id;
    }

    public int getFromFloor()
    {
        return fromFloor;
    }

    public int getToFloor()
    {
        return toFloor;
    }

    public Direction getDirection()
    {
        return direction;
    }

    @Override
    public String toString()
    {
        return "Request#" + id + " { from=" + fromFloor + ", to=" + toFloor + ", dir=" + direction + " }";
    }
}
