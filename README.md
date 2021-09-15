# Design & Implementation
- I have created a class `CoffeeMachine` representing the coffee machine.
- It has a property `Map<String, Integer> ingredientContainer` representing the state of ingredients inside the coffee machine
- It also has a thread pool executor `ExecutorService executorService`. This thread pool executor handles the workers that will be submitted to dispense beverage.
- Each worker can independently check the state of ingredients anytime. But while performing update (Collecting Ingredients) the workers have to acquire a lock. During this process no other thread can update or read the state of ingredients.
- This lock is implemented using property `ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock()`
- The class also has a method `getBeverage` that takes the order and abstract all internal working
- The internal working to dispense beverage is handled by method `dispenseBeverage`.
- I have also added a 2 seconds delay simulating actual coffee being made after ingredients are collected.
- There are 2 more threads `Thread indicatorService` and `Thread refillService`.
- The indicator service continuously reads the state of Coffee Machine and writes whether a particular ingredient is running low. It logs the details in `resources/indicators.txt`.
- The refill service continuously checks a file `resources/refill.txt` if it contains any entry in following format: `<ingredientName>:<amount>`, the service tries to refill the given amount.
- I have assumed the initial values of ingredient are the maximum capacity of the container. These are stored in `Map<String, Integer> containerSizes` while creating the initial state of the machine.

# Execution
- I have used one external library `jackson:2.2.3` to parse the json file
- We need to compile the source code using below command
   ```bash
   javac -cp lib/jackson-annotations-2.2.3.jar:lib/jackson-core-2.2.3.jar:lib/jackson-databind-2.2.3.jar:. CoffeeMachine.java
   ```
- I have created example json files in `resources/` directory
- We just run the `CoffeeMachine` class and give argument to the input file we want to use as below:
   ```bash
   java -cp lib/jackson-annotations-2.2.3.jar:lib/jackson-core-2.2.3.jar:lib/jackson-databind-2.2.3.jar:. CoffeeMachine resources/test1.json
   ```

# Test Cases
1. All ingredients are available and sufficient - Valid
2. Some ingredients are insufficient - Valid
3. Some ingredients are unavailable - Valid
4. Some ingredients are insufficient and unavailable - Valid
5. All ingredients are insufficient - Valid
6. All ingredients are unavailable - Valid
7. No outlets available - Invalid