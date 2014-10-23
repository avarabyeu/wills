# wills [![Build Status](https://travis-ci.org/avarabyeu/wills.svg?branch=master)](https://travis-ci.org/avarabyeu/wills)

Useful utils built on top of Guava's ListenableFuture


* [Maven Dependencies](#maven-dependencies)
* [The problem 'wills' solve](#the-problem-'wills'-solve)
* [Creation](#creation)
    * [from JKSs Future](#from-jdks-future)
    * [from Guava's Future](#from-guava's-listenablefuture)
    * [Decorating ExecutorService](#decorating-executorservice)
* [Callbacks](#callbacks)
    * [whenSuccessful](#whenSuccessful)
    * [whenFailed](#whenFailed)
    * [whenDone](#whenDone)
* [Fallback (Replacing Future in case of failure)](#Fallback-Replacing-Future-in-case-of-failure) 

## Maven Dependencies
Last stable version:
```xml
<dependency>
    <groupId>com.github.avarabyeu</groupId>
    <artifactId>wills</artifactId>
    <version>0.0.1</version>
</dependency>
```

## The problem 'wills' solve
Google did perfect job releasing Guava - set of very useful utilities for collections, I/O, concurrency and others java APIs.
Guava's concurrency utilities helps to solve widely used cases such as adding callbacks, transforming/filtering futures. 
But, sometimes it looks a bit ridiculous. Just take a look:

```java
ListeningExecutorService executorService = MoreExecutors
            .listeningDecorator(Executors.newFixedThreadPool(1));
ListenableFuture<String> future = executorService
            .submit(new SomeCallable<String>());
Futures.addCallback(future, new FutureCallback<String>() {
    @Override
    public void onSuccess(@Nullable String result) {
        System.out.println("I'm successful!");
    }
    @Override
    public void onFailure(Throwable t) {
            System.out.println("I'm not!");
    }
});

```

Too much lines of code, right? Adding callbacks via Futures make sense if you want to add several ones. But what if you need to add just one?
This is what 'wills' do for you:

```java
WillExecutorService executorService = WillExecutors
            .willDecorator(Executors.newFixedThreadPool(1));
Will<String> future = executorService
            .submit(new SomeCallable<String>())
.whenSuccessful(new Action<String>() {
    @Override
    public void apply(String s) {
        System.out.println("I'm OK!");
    }
}).whenFailed(new Action<Throwable>() {
    @Override
    public void apply(Throwable throwable) {
        System.out.println("I'm not!");
    }
});
```
or even more simple in JDK8 style:

```java

executorService.submit(new SomeCallable<String>())
     .whenSuccessful(result -> System.out.println("I'm OK!"))
     .whenFailed(throwable -> System.out.println("I'm not!"));

```

Another words, **using 'wills' you are able to build chained method calls** which makes code cleaner and faster to implement.
Actually, com.github.avarabyeu.wills.Will interface extends Guava's ListanableFuture with some useful convenience methods.
Go through documentation to find out explanation about them.

## Creation

### from JDKs Future
### from Guava's ListenableFuture
### Decorating ExecutorService

## Callbacks

### whenSuccessful
Executes some action once Future is done.

Example:
```java
Will<String> will = Wills.of("SOME RESULT")
    .whenSuccessful(new Action<String>() {
        @Override
        public void apply(String o) {
            doSomething():
        }
});

```
Example (JDK8):
```java
Wills.of("SOME RESULT").whenSuccessful(result -> doSomething());

```


Guava's analogue:

```java
ListenableFuture<?> future = Futures.immediateFuture("SOME RESULT");
Futures.addCallback(future, new FutureCallback<Object>() {
    @Override
    public void onSuccess(@Nullable Object result) {
        doSomething();
    }

    @Override
    public void onFailure(Throwable t) {
        //do nothing
    }
});
```

### whenFailed
Executes some action in case Future is failed

Example:
```java
Will<String> will = Wills.of("SOME RESULT")
    .whenFailed(new Action<String>() {
        @Override
        public void apply(String o) {
            doSomething():
        }
});

```
Example (JDK8):
```java
Wills.of("SOME RESULT").whenFailed(result -> doSomething());

```


Guava's analogue:

```java
ListenableFuture<?> future = Futures.immediateFuture("SOME RESULT");
Futures.addCallback(future, new FutureCallback<Object>() {
    @Override
    public void onSuccess(@Nullable Object result) {
        //do nothing
    }

    @Override
    public void onFailure(Throwable t) {
        doSomething();
    }
});
```

### whenDone
Executes some action once Future is completed. Doesn't matter successful or not.
Here action is boolean-type, because there will be passed execution result (TRUE in case if future execution is successful)

Example:
```java
Will<String> will = Wills.of("SOME RESULT")
    .whenDone(new Action<Boolean>() {
        @Override
        public void apply(Boolean successful) {
            doSomething():
        }
});

```
Example (JDK8):
```java
Wills.of("SOME RESULT").whenDone(successful -> doSomething());

```


Guava's analogue: There is no direct analogue. You can only can add the following callback:

```java
new FutureCallback<A>() {
    @Override
    public void onSuccess(@Nullable A result) {
        action.apply(true);
    }

    @Override
    public void onFailure(Throwable t) {
        action.apply(false);
    }
});
```

## Fallback (Replacing Future in case of failure)