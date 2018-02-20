package org.isep.maw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;


public class CSVTweetSpout extends BaseRichSpout {
	
	public static int MAX_QUEUE_SZ= 200;
	public static int THRESOLD = 100;
	
	
	
	private static final long serialVersionUID = 1L;
	
	private SpoutOutputCollector _collector;
	private List<BufferedReader> scanList = new ArrayList<BufferedReader>();
	
	private final List<String> fileList;
	private final Queue<Tweet> recQ = new PriorityQueue<Tweet>(MAX_QUEUE_SZ);
	private final String separator;
	
	
	public CSVTweetSpout(String separator, List<String> fileList) throws FileNotFoundException {
		this.fileList = fileList;
		this.separator = separator;
	}
	
	
	private void init(List<String> fileList) throws FileNotFoundException {
		for(String fileName: fileList) {
			Path f  = Paths.get(fileName);
			
			if(f.toFile().exists()) {
				System.out.println("OPENING: " + fileName);
				BufferedReader br = null;
				try {
					br = Files.newBufferedReader(f, Charset.forName("UTF-8"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(br!=null)
					scanList.add(br);
			}
		}
	}
	@Override
	public void nextTuple() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Refill the list
		if(recQ.size() < THRESOLD ) { // Read from scanners.
			
			while(recQ.size() < MAX_QUEUE_SZ && scanList.size() > 0) {
				ListIterator<BufferedReader> it = scanList.listIterator();
				
				while(it.hasNext()) {
					
					BufferedReader br = it.next();
					
					String tweetStr = null;
					try {
						tweetStr = br.readLine();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(tweetStr !=null) {
						Tweet t = scanTweet(tweetStr);
						if(t != null) recQ.offer(t);
						
					}else {
						System.out.println("Nothing to read");
						try {
							br.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						it.remove();
					}
				}
			}
		}
		
		// emit next tweet
		Tweet tweet = recQ.poll();
		if(tweet != null ) {
			//System.out.println(tweet.getId() + " " + tweet.getText() );
			_collector.emit(new Values(tweet.getId(),tweet.getCreationDate(), tweet.getText()));
		}
	}

	private Tweet scanTweet(String  line) {
		
		System.out.println(line);
		StringTokenizer st = new StringTokenizer(line,separator,false);
		Tweet t = null;
		try {
			
			//System.out.println("==== TEST ==== " + st.nextToken());
		 t = new Tweet(Long.parseLong(st.nextToken()), Long.parseLong(st.nextToken()), st.nextToken());
		} catch (Exception e) {
			System.out.println(e.toString() + " " + e.getMessage());
		}
		return t;
	}


	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		// TODO Auto-generated method stub
		this._collector = arg2;
		try {
			init(fileList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("id","ts","text"));
		
	}

}
