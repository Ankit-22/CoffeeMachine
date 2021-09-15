import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class RefillTask implements Runnable{

    private final CoffeeMachine machine;

    public RefillTask(CoffeeMachine machine) {
        this.machine = machine;
    }

    @Override
    public void run() {
        while(true) {
            try {
                String refillInstruction = new Scanner(new File("resources/refill.txt")).useDelimiter("\\Z").next();
                String name = refillInstruction.split(":")[0];
                Integer amount = Integer.parseInt(refillInstruction.split(":")[1]);
                this.machine.refillIngredient(name, amount);
            } catch (IOException ignored) {
            }
        }
    }
}
