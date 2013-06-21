taskaroo
========

A Java library that handles execution of tasks. Tasks can be executed sequentially, in parallel, or in any order.

**Example**

```java
// A task to search a massive haystack for the first occurrence of needle.
public class SearchTask extends Task<Integer> {
    private String needle, haystack;
    public SearchTask(String needle, String haystack) {
        this.needle = needle;
        this.haystack = haystack;
    }
    protected Integer execute() {
        return haystack.indexOf(needle);
    }
}

// Create a service for performing the tasks.
TaskService service = new TaskService();
service.start();

// Create the search task and set the service which will execute it.
SearchTask task = new SearchTask("o", "Hello World");
task.setHandler(service);
// Wait no longer then a second when synchronously invoking.
task.setTimeout(1000);

// Execute the task asynchronously.
task.async(new TaskListener<Integer>() {
    public void onTaskTimeout(Task<Integer> source) {
        // Task timed out (took more than a second).
    }
    public void onTaskSuccess(Task<Integer> source, Integer result) {
        // Task was a success and no errors were thrown.
    }
    public void onTaskFinish(Task<Integer> source) {
        // Task has completed in some manor.
    }
    public void onTaskError(Task<Integer> source, Throwable error) {
        // Task produced an error (NullPointerException?)
    }
    public void onTaskCancel(Task<Integer> source) {
        // Task cancelled before it ever finished.
    }
});

// Execute task synchronously (could timeout if it takes more than a second).
Integer result = task.sync();

// Stop handling tasks.
service.stop();
```

**Builds**

https://github.com/ClickerMonkey/taskaroo/tree/master/build

**Testing Examples**

https://github.com/ClickerMonkey/taskaroo/tree/master/Testing/org/magnos/task
