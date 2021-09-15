public class IndicatorTask implements Runnable {

    private CoffeeMachine machine;

    public IndicatorTask(CoffeeMachine machine) {
        this.machine = machine;
    }

    @Override
    public void run() {
        while(true) {
            machine.checkIngredientsState();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
