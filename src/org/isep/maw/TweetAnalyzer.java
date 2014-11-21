package org.isep.maw;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import com.google.common.collect.Lists;

public class TweetAnalyzer {
	static private int TOP = 10;
	static private int EMIT_FREQ = 5;
	static private int WINDOW_SIZE = 15;
	
	
	private static LocalCluster cluster; 
	public static void main(String [] args) throws FileNotFoundException {
		cluster = new LocalCluster();
		List<String> fileList = Lists.newArrayList(args);		
		
		List<String> pathList = new ArrayList<String>();
		
		
		for(String relPath: fileList) {
			File f = new File(relPath);
			pathList.add(f.getAbsolutePath());

		}
		
		
		TopologyBuilder builder = new TopologyBuilder();
		
		builder.setSpout("tweet", new CSVTweetSpout(pathList));
		builder.setBolt("wordcount", new WordCountBolt(),3)
					.shuffleGrouping("tweet");
		builder.setBolt("rank", new RollingWordRank(TOP,EMIT_FREQ,WINDOW_SIZE),3) 
					.fieldsGrouping("wordcount", new Fields("word"));
		builder.setBolt("totalranking", new MergeWordsRanks(TOP, EMIT_FREQ, new File("test-results.json").getAbsolutePath()),1)
					.globalGrouping("rank");
		
		
		//1. build topology
		Config conf = new Config();
		conf.setDebug(true);
		//2. run it for a while
		cluster.submitTopology("test", conf, builder.createTopology());
		try {
			Thread.sleep(27500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cluster.shutdown();		
		
	}
}
