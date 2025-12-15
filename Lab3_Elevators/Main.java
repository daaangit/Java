import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// elevators and dispatcher start + req generation
public class Main {

    public static void main(String[] args) throws Exception
    {
        final int N_FLOORS = 16;          // floors: 0->15
        final int M_ELEVATORS = 3;
        final int CAPACITY = 5;

        final long MOVE_STEP_MS = 350;    // each floor travel time
        final long DOORS_MS = 800;        // doors openimg time

        BlockingQueue<Request> incoming = new LinkedBlockingQueue<>();

        List<Elevator> elevators = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < M_ELEVATORS; i++)
        {

            Elevator e = new Elevator(
                    i + 1,
                    0,
                    N_FLOORS - 1,
                    CAPACITY,
                    0,
                    MOVE_STEP_MS,
                    DOORS_MS
            );
            elevators.add(e);
            Thread t = new Thread(e, "Elevator-" + (i + 1));
            t.start();
            threads.add(t);
        }

        Dispatcher dispatcher = new Dispatcher(elevators, incoming);
        Thread dispatcherThread = new Thread(dispatcher, "Dispatcher");
        dispatcherThread.start();
        threads.add(dispatcherThread);

        // request gen

        Thread generator = new Thread(() -> {
            Random rnd = new Random();
            while (!Thread.currentThread().isInterrupted())
            {
                try {
                    int from = rnd.nextInt(N_FLOORS);
                    int to;
                    do {
                        to = rnd.nextInt(N_FLOORS);
                    } while (to == from);

                    Request r = new Request(from, to);
                    Log.info("Generator", "New " + r);
                    incoming.put(r);

                    Thread.sleep(1200);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception ex) {
                    Log.info("Generator", "ERROR: " + ex.getMessage());
                }
            }
        }, "Generator");
        
        generator.start();
        threads.add(generator);

        // overall simulation time
        Thread.sleep(20_000);

        Log.info("Main", "Stopping simulation...");
        for (Thread t : threads) t.interrupt();
        for (Thread t : threads) t.join();
        Log.info("Main", "Done");
    }
}
