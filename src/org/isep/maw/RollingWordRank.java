package org.isep.maw;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;



public class RollingWordRank extends BaseRichBolt {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -881299232569737024L;
	private OutputCollector _collector;
	private final long emitFrequency, windowLength;
	private final int nbBatches, top;
	private int topEmitted = 0;
	
	Map<String, Integer[]> wordMap =new HashMap<String, Integer[]>();
	
	public RollingWordRank(int top, long emitFrequency, long windowLength) {
		this.top = top;
		this.nbBatches = (int) (windowLength / emitFrequency);
		this.emitFrequency = emitFrequency;
		this.windowLength = windowLength;
	}
	
	/**
	 * Checks whether it is a tick tuple 
	 * Adds the count to the word,
	 */
	@Override
	public void execute(Tuple tuple) {
	
		if(Utils.isTickTuple(tuple)) {
			emitAndFlush();
			
		} else {
			String word = tuple.getString(0);
			
			if(wordMap.containsKey(word)) {
				
				Integer[] window = wordMap.get(word);
				for(int i=0;i<window.length;i++) {
					window[i] += tuple.getInteger(1);
				}
				wordMap.put(word, window);
			} else {
				Integer [] window = new Integer[nbBatches];
				Arrays.fill(window, tuple.getInteger(1));
				wordMap.put(word,window);
			}
		}
		_collector.ack(tuple);
	}
	
	

	private void emitAndFlush() {
		
		int index = topEmitted++ % nbBatches;
		TreeSet<TopWord> sortedWords = new TreeSet<TopWord>();
		//1. Sort the words
		Iterator<Entry<String, Integer[]>> it = wordMap.entrySet().iterator();
		
		while(it.hasNext()) {
			Entry<String, Integer[]> e = it.next();
			Integer[] window = e.getValue();
			int max = Utils.max( window);
			
			// Update the window and set it back
			
			if(max==0) {
				it.remove();
			} else {
				window[index] = 0;
				e.setValue(window);
				// Put it in the top
				TopWord tw = new TopWord();
				tw.count = max;
				tw.word = e.getKey();
				sortedWords.add(tw);
				
				
			}
			
		}		
		//2. Prune the sorted word list
		while(sortedWords.size() > top) {
			sortedWords.pollLast();
		}

		_collector.emit(new Values(sortedWords));

	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		
		_collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("ranking"));
		
	}
	
	
	 @Override
	 public Map<String, Object> getComponentConfiguration() {
		 Map<String, Object> conf = new HashMap<String, Object>();
		 conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequency);
		 return conf;
	 }

}
