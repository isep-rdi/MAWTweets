MAWTweets
=========

Storm tweet word count

This project relies on D3.js and the d3 word cloud by Jason Davies for result visualisation (https://github.com/jasondavies/d3-cloud).

Running
=======
Usage: $ tweetAnalyzer top_n path_result [;|,] path_to_data1 path_to_data2 ..." 

Example:
```
java -cp MAWTWeets.jar org.isep.maw.TweetAnalyzer 10 ./visualisation/result.json , ./dataset/test.csv
```

