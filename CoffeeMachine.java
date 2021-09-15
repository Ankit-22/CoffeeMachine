import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;

@SuppressWarnings("unchecked")
public class CoffeeMachine {

    // This property will hold the state of each ingredient
    private final Map<String, Integer> ingredientContainer = new HashMap<>();
    private final Map<String, Integer> containerSizes = new HashMap<>();

    // This is the lock workers will use while removing ingredients
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // This is the service that will handle ThreadPool workers(dispensers)
    private final ExecutorService executorService;

    // This is the thread that runs the indicator service
    private final Thread indicatorService;

    // This is the thread that runs the refill service
    private final Thread refillService;

    public CoffeeMachine(Integer dispensers, List<Ingredient> ingredients) {
        // Initialize the executor service with number of workers = number of dispensers
        executorService = Executors.newFixedThreadPool(dispensers);

        // Initialize the indicator and refill services
        indicatorService = new Thread(new IndicatorTask(this));
        indicatorService.start();
        refillService = new Thread(new RefillTask(this));
        refillService.start();

        // Create the initial state of the IngredientContainer.
        for(Ingredient ingredient: ingredients) {
            ingredientContainer.put(ingredient.getName(), ingredient.getAmount());
            containerSizes.put(ingredient.getName(), ingredient.getAmount());
        }
    }

    // Method simply submits a list of tasks from the order to the executorService
    public void getBeverages(List<BeverageTask> tasks) throws InterruptedException {
        executorService.invokeAll(tasks);
        // Shutdown when all tasks are completed
        indicatorService.stop();
        refillService.stop();
        executorService.shutdown();
    }

    /*
        This method is called from the worker(dispenser) to actually complete the task
        I have assumed that we have to make this process transactional to avoid wastage
        E.g. If a beverage is made from tea: 10, milk: 12 and sugar: 3 and there is not enough sugar,
        No amount of milk or tea should be reduced. To achieve this we have to ensure no other workers read or write
        to the IngredientContainer during this process.
        This method is protected because we only want trusted objects (in same package) should be able to call it.
     */
    protected boolean dispenseBeverage(Beverage beverage) {
        // Acquire write lock to ensure no other workers are reading or writing
        rwLock.writeLock().lock();
        if(this.canMakeBeverage(beverage)) {
            this.makeBeverage(beverage);
        } else {
            rwLock.writeLock().unlock();
            return false;
        }
        // Release the lock after transactional part is completed
        rwLock.writeLock().unlock();

        // After getting all the ingredients let's make beverage by mixing, heating and stirring.'
        // Wait for 2 seconds to make the beverage
        try {
            System.out.println(format("%s is being prepared", beverage.getName()));
            Thread.sleep(2000);
            System.out.println(format("%s is prepared", beverage.getName()));
        } catch (InterruptedException e) {
            System.out.println("Please repair the Coffee Machine");
        }
        return true;
    }

    private boolean canMakeBeverage(Beverage beverage) {
        // Just check if we have enough ingredients to make beverage
        for(Ingredient ingredient: beverage.getIngredients()) {
            if(Objects.isNull(ingredientContainer.get(ingredient.getName()))) {
                // Inform if ingredient unavailable
                System.out.println(format("%s cannot be prepared because %s is not available%n",
                        beverage.getName(), ingredient.getName()));
                return false;
            }
            if(ingredient.getAmount() > ingredientContainer.get(ingredient.getName())) {
                // Inform if ingredient insufficient
                System.out.println(format("%s cannot be prepared because item %s is not sufficient%n", beverage.getName(), ingredient.getName()));
                return false;
            }
        }
        return true;
    }

    private void makeBeverage(Beverage beverage) {
        // Actually make the beverage (by getting all the ingredients)
        for(Ingredient ingredient: beverage.getIngredients()) {
            Integer availableIngredient = ingredientContainer.get(ingredient.getName());
            ingredientContainer.put(ingredient.getName(), availableIngredient - ingredient.getAmount());
        }
    }

    // Method stores the status of each ingredient in the file resource/indicator.txt
    protected void checkIngredientsState() {
        try {
            BufferedWriter indicator = new BufferedWriter(new FileWriter("resources/indicator.txt"));
            rwLock.readLock().lock();
            Set<String> ingredientNames = ingredientContainer.keySet();
            rwLock.readLock().unlock();
            for(String name : ingredientNames) {
                rwLock.readLock().lock();
                indicator.write(String.format("%s: %s", name,
                        (ingredientContainer.get(name) <= 0.2 * containerSizes.get(name)) ? "True\r\n" : "False\r\n"));
                rwLock.readLock().unlock();
            }
            indicator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method can be used to refill the ingredients
    protected void refillIngredient(String name, Integer amount) {
        rwLock.writeLock().lock();
        if(amount + ingredientContainer.get(name) >= containerSizes.get(name)) {
            ingredientContainer.put(name, containerSizes.get(name));
        } else {
            ingredientContainer.put(name, amount + ingredientContainer.get(name));
        }
        rwLock.writeLock().unlock();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        /*
            Verify that user has provided a file argument to check for test cases
            If user has not entered a valid file, IOException will be thrown
         */
        if(args.length != 1  || !Objects.nonNull(args[0])) {
            System.out.println("Please read the README.md file to know how to use this application.");
        }

        /*
            Get the json from input file into a string
            Then convert it into a generic json object (Map<String, Object>)
         */
        String input = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonInput = objectMapper.readValue(input, new TypeReference<Map<String, Object>>(){});

        /*
            Extract the initial state of coffeeMachine from json
            and initialize the coffeeMachine
         */
        Map<String, Object> machine = (Map<String, Object>) jsonInput.get("machine");
        Integer dispensers = (Integer) ((Map<String, Object>)  machine.get("outlets")).get("count_n");
        List<Ingredient> ingredients = Utils.createIngredients((Map<String, Object>) machine.get("total_items_quantity"));
        CoffeeMachine coffeeMachine = new CoffeeMachine(dispensers, ingredients);

        /*
            Extract list of beverage orders from json and submit them to the machine.
            The machine has a threadPool in which these tasks get submitted
            and will be picked by any worker that is free
         */
        List<Beverage> beverages = Utils.createBeverages(((Map<String, Object>) machine.get("beverages")));
        List<BeverageTask> tasks = new ArrayList<>();
        for(Beverage beverage: beverages) {
            tasks.add(new BeverageTask(coffeeMachine, beverage));
        }
        coffeeMachine.getBeverages(tasks);
    }
}
