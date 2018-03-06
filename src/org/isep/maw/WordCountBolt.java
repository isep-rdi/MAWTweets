package org.isep.maw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;



public class WordCountBolt implements IRichBolt  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6717400697736283258L;
	Set<String> excluded = new HashSet<>();
	{
		excluded.add("christmas");
	}
	
	OutputCollector _collector;

	
	@Override
	public void execute(Tuple input) {
		String tweetText = input.getString(2);
		
		
		//1. Filter out with stop words and count.
		Map<String, Integer> wordCountMap = new HashMap<String, Integer>();
		String [] words = tweetText.toLowerCase().split(" ");
	
		for(String word: words) {
			if((word.matches("^[a-z]+$")) 
					&& word.length() > 4
					&& !excluded.contains(word)) {
				int val = wordCountMap.containsKey(word) ? wordCountMap.get(word) + 1 : 1;
				wordCountMap.put(word, val);
			}
		}
		
		//2. emit results:
		for(Entry<String,Integer> e: wordCountMap.entrySet()) {
		    _collector.emit(input, new Values(e.getKey(), e.getValue()));
		}
	    _collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    declarer.declare(new Fields("word", "count"));
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		_collector = arg2;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
