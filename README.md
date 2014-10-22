# wills [![Build Status](https://travis-ci.org/avarabyeu/wills.svg?branch=master)](https://travis-ci.org/avarabyeu/wills)

Useful utils built on top of Guava's ListenableFuture


* [Maven Dependencies](#maven-dependencies)

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
Guava's concurrency utilities helps to solve widely used problems such as adding callbacks very fast and productive. 
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

Too much lines of code, right? Adding callbacks via Futures makes sense if you want to add several ones. But what if we need to add just one?
This is what 'wills' do for you:

```java
WillExecutorService executorService = WillExecutors
            .willDecorator(Executors.newFixedThreadPool(1));
Will<String> future = executorService
            .submit(new SomeCallable<String>())
.whenDone(new Action<String>() {
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
     .whenDone(result -> System.out.println("I'm OK!"))
     .whenFailed(throwable -> System.out.println("I'm not!"));

```

Another words, using 'wills' you are able to build chained method calls which makes code cleaner and faster to implement.
Actually, com.github.avarabyeu.wills.Will interface extends Guava's ListanableFuture with some useful convenience methods.
Go through documentation to find out explanation about them.