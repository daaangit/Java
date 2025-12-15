/**
 * A single passenger task assigned to an elevator.
 * For simplicity we assume one passenger per request.
 */
public final class PassengerTask
{
    public final Request request;
    public boolean pickedUp;

    public PassengerTask(Request request)
    {
        this.request = request;
        this.pickedUp = false;
    }
}
