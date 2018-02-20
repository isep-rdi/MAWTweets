package org.isep.maw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;


public class TweetAnalyzer {
	static private int TOP = 20;
	static private int EMIT_FREQ = 30;
	static private int WINDOW_SIZE = 60;
	
	private static LocalCluster cluster; 
	public static void main(String [] args) throws FileNotFoundException {
		if(args.length <= 3) {
			System.out.println("Usage: $ tweetAnalyzer top_n path_result [;|,] path_to_data1 path_to_data2 ..." );
			System.exit(0);
		}
		TOP = Integer.parseInt(args[0]);
		String resultFilePath = args[1];
		String separator = args[2];
		
		cluster = new LocalCluster();
		List<String> fileList = Arrays.asList(args).subList(2, args.length);
		List<String> pathList = new ArrayList<String>();	
		
		for(String relPath: fileList) {
			File f = new File(relPath);
			pathList.add(f.getAbsolutePath());
		}
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("tweet", new CSVTweetSpout(separator,pathList));
		builder.setBolt("wordcount", new WordCountBolt(),3)
					.shuffleGrouping("tweet");
		builder.setBolt("rank", new RollingWordRank(TOP,EMIT_FREQ,WINDOW_SIZE),3) 
					.fieldsGrouping("wordcount", new Fields("word"));
		builder.setBolt("totalranking", new MergeWordsRanks(TOP, EMIT_FREQ, new File(resultFilePath).getAbsolutePath()),1)
					.globalGrouping("rank");		
		//1. build topology
		Config conf = new Config();
		conf.setDebug(true);
		//2. run it for a while
		cluster.submitTopology("TWEET 2018", conf, builder.createTopology());
		try {
			Thread.sleep(47500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cluster.shutdown();		
		
	}
}
