import java.util.concurrent.Callable;

public class BeverageTask implements Callable<Boolean> {

    private final CoffeeMachine machine;
    private final Beverage beverage;

    public BeverageTask(CoffeeMachine machine, Beverage beverage) {
        this.machine = machine;
        this.beverage = beverage;
    }

    @Override
    public Boolean call() {
        // Simply use the protected method inside the CoffeeMachine that dispenses a Beverage
        return machine.dispenseBeverage(beverage);
    }
}
