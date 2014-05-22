Since March 2014, Java 8 has been available. To be honest, I expected more fuzz about this release. This release is one of the biggest releases so far and many people will enjoy features that are bundled with it (like me).

This post will not describe all new features in JDK8 (like lambda's, streams, ...). It will describe on how to use it. If you want more information, you can find some very good explanations on the internet. Definitely checkout some talks of Venkat Subramaniam and José Paumard. You can find some talks of them on [parleys][1].

## Problem description

To start with, what will we program? Well, I found a very interesting website that posts the ["mega millions" results][2]. On that page you will see that there is a select box available where you can select the "draw" and you will see the results.

We can use the information on this website to see for example which numbers were picked most.

I will not go into detail on how to scrape out the information on the website. If you are interested, the code is available on my github account.

## Solution

I have 2 classes at my disposal which I can use, LotterDraws and LotterDraw. The first gives me the list of all "draws" with an id, so basically the list of all the values in the select box. With the second class I can retrieve the results for one draw based on the id.

![Lotter class diagram][3]

## Get all numbers

To start with, we will get all drawn numbers into one big list. This list will basically contain all numbers that were drawn ever on mega-millions.

We will get all the lotter draws (all the values from the select list) and put it into a **stream**:

    lotterDraws.getDraws()
        .stream()
    

To "visualize" the result, we have something like this in our stream (where each number is the id of a draw):

    [13,8,7,4,3,5,6,1,9,2,10,12,11,...]
    

This created a stream of Integer's. We can **sort** them to get them in the order of the id (Integer):

    lotterDraws.getDraws()
        .stream()
        .sorted()
    

Result:

    [1,2,3,4,5,6,7,8,9,10,11,12,13,...]
    

While testing the code (so we run it often), we don't want to fetch all the results all the time (when we know everything is correct we can do this). So we can **limit** the number of draws that we will fetch:

    lotterDraws.getDraws()
            .stream()
            .sorted()
            .limit(10)
    

Result:

    [1,2,3,4,5,6,7,8,9,10]            
    

In the next step, we need to get the results (list of numbers) for a certain draw based on id. So for every Integer that have now in our stream, we have to call "LotterDraw.getDraw". We can **map** every draw id that we have now in our list into a list of numbers (numbers that were drawn for this draw).

    lotterDraws.getDraws()
        .stream()
        .limit(10)
        .sorted()
        .map(lotterDraw::getDraw)
    

Here we used a method reference for "getDraw". Now we have a stream containing a list of integers:

    [ [6, 13, 18, 27, 45, 18], [12, 28, 45, 46, 52, 47] ,[3, 25, 29, 30, 48, 48, 2], ... ]
    

Last step, merge all lists into one big list. We can **reduce** our stream to an ArrayList:

    lotterDraws.getDraws()
        .stream()
        .sorted()
        .limit(10)
        .map(lotterDraw::getDraw)
        .reduce(new ArrayList<>(), (a, b) -> {
            a.addAll(b);
            return a;
        });
    

Maybe a bit of explanation on this reduce function. The first parameter is called the identity (this is the starting value). It will go through each item in our stream and take the identity and the item in our stream and apply a binary function onto it (called the accumulator). Here, we added elements together on one list. So we reduced our stream of lists of integers into a list of integers!

It is the equivalent of (this explanation comes directly from the javadoc):

      T result = identity;
      for (T element : this stream)
          result = accumulator.apply(result, element)
      return result;
    

Final result is the list of integers:

    [6, 13, 18, 27, 45, 18, 12, 28, 45, 46, 52, 47, 3, 25, 29, 30, 48, 48, ... ]
    

* * *

## Parallelisation

In previous chapter, we were able to get the results for 10 draws. But we want it for all draws. You can try this, just remove the "limit" part in previous example. Suddenly, things get very slooow. Getting the results for all draws might take some time. Webservers are able to process multiple requests at the same time, so why not fetch the data in parallel? Luckely this is a no-brainer using streams:

    lotterDraws.getDraws()
        .stream()
        .parallel()
        .map(lotterDraw::getDraw)
        .reduce(new ArrayList<>(), (a, b) -> {
            a.addAll(b);
            return a;
        });
    

As you can see I removed the limit and sort in example above (sorting does not make much sense when we will process it in parallel).

As you can see, concurrency is very using when you are using streams but **be careful**! You should **always** test this properly, in most cases it would probably take longer due to the overhead that comes with it (for simple calculations). Don't use it because it is easy to use it, use it when you need it and you tested it. I wanted to show the parallel feature of streams so I knew up front that this example would be appropriate for using it. For people that are interested in this topic: under the hood the [fork join framework][4] is used for parallel processing.

> Junior programmers think concurrency is hard.
> 
> Experienced programmers think concurrency is easy.
> 
> Senior programmers think concurrency is hard.

* * *

## Statistics

Now that we have our list of all drawn numbers, we can do some interesting stuff on it. We will start by again creating a stream of our big list of drawn numbers:

    allNumbers
        .stream()
    

Where allNumbers is the list that was the result of the previous chapter.

Now we want to perform a **group by** operation on this list (by the number itself) and count the number of times the number was present. SQL equivalent would be:

    SELECT n, count(*)
    FROM   allNumbers
    group by n
    

Using streams this gives us:

    allNumbers
        .stream()
        .collect(
                Collectors.groupingBy(
                        x -> x,
                        Collectors.counting()
                )
        )
    

So we collected the stream of Integer's into a Map<Integer,Integer>. The Collectors.counting() was used as a downstream collector. Example result:

    {64=32, 1=96, 2=32, 66=48, 3=48, 69=32, 5=32, 70=32, 7=48, 9=64,...}
    

So the number 64 was drawn 32 times, the number 1 96 times, ...

We actually have the result already available now if anybody noticed! But it is not quite handy, let's sort it based on the number of times a number was present and put it in a list of map entries.

For this, we will use the **Set<Map.Entry<K, V>> entrySet()** of the Map created above:

    allNumbers
        .stream()
        .collect(
                Collectors.groupingBy(
                        x -> x,
                        Collectors.counting()
                )
        )
    .entrySet()
        .stream()
    

On this stream, we will sort using a comparator based on the value of the map entry. Luckely, a comparator is already available for us to use:

    allNumbers
        .stream()
        .collect(
                Collectors.groupingBy(
                        x -> x,
                        Collectors.counting()
                )
        )
    .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
    

In the final step, we will collect this into a list and we are done:

    List<Map.Entry<Integer, Long>> statistics =
        allNumbers
            .stream()
            .collect(
                    Collectors.groupingBy(
                            x -> x,
                            Collectors.counting()
                    )
            )
        .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toList());
    

This gives us:

    [16=8, 56=8, 64=32, 2=32, 69=32, 5=32, 70=32, 74=32, 19=32, 25=32, 26=32, 27=32, 28=32, 35=32, 37=32, ...]
    

(When getting results in all examples above, I only used a subset of all draws: limit(10)).

* * *

## Conclusion

As you can see, working with streams and lambda's is quite powerful. It is also much more readable, you can almost read what you are doing. When you have to loop over everything all the time, things can get messy really fast. But to be very honest, it is still not as readable as Scala or any other functional programming language (altough this is very subjective). When using this aspact of Java8, you really will have to be an API expert to get the full benefit!

Example code is available on my [github][5] account. So feel free to have a look.

 [1]: http://parleys.com/home
 [2]: http://www.thelotter.com/lottery-results/usa-megamillions
 [3]: https://jacobsvanroy.be/blog/wp-content/uploads/2014/05/lotter_class_diagram.png
 [4]: http://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
 [5]: https://github.com/davyvanroy/jdk8-lotter