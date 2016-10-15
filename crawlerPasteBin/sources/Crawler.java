import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author Esad
 * @Think to remove this bitches
 * 
 * Class to crawler new PasteBin posts
 */
public class Crawler {
	/**
	 * Url site
	 */
	private static final String baseUrl = "http://pastebin.com";
	/**
	 * Base time to sleep
	 * 10 sec
	 */
	private static final int sleepTime = 1000;
	/**
	 * Sleep time when we havn't find new paste
	 */
	private static final int notFoundSleepTime = 10000;
	/**
	 * User agent send
	 */
	private  final static String[] userAgentList = {"Mozilla/5.0 (Linux; Android 6.0.1; SM-G920V Build/MMB29K) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/52.0.2743.98 Mobile Safari/537.36","Mozilla/5.0 (Linux; Android 5.1.1; SM-G928X Build/LMY47X) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/47.0.2526.83 Mobile Safari/537.36","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/13.10586","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/47.0.2526.83 Mobile Safari/537.36","Mozilla/5.0 (Linux; Android 6.0.1; E6653 Build/32.2.A.0.253) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/52.0.2743.98 Mobile Safari/537.36","Mozilla/5.0 (Linux; Android 6.0; HTC One M9 Build/MRA58K) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/52.0.2743.98 Mobile Safari/537.36","Mozilla/5.0 (Linux; Android 7.0; Pixel C Build/NRD90M; wv) ","AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 ","Chrome/52.0.2743.98 Safari/537.36","Mozilla/5.0 (Linux; Android 6.0.1; SGP771 Build/32.2.A.0.253; wv) ","AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 ","Chrome/52.0.2743.98 Safari/537.36","Mozilla/5.0 (Linux; Android 5.1.1; SHIELD Tablet Build/LMY48C) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/52.0.2743.98 Safari/537.36","Mozilla/5.0 (Linux; Android 5.0.2; SAMSUNG SM-T550 Build/LRX22G) ","AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/3.3 ","Chrome/38.0.2125.102 Safari/537.36","Mozilla/5.0 (Linux; Android 4.4.3; KFTHWI Build/KTU84M) ","AppleWebKit/537.36 (KHTML, like Gecko) Silk/47.1.79 like ","Chrome/47.0.2526.80 Safari/537.36","Mozilla/5.0 (Linux; Android 5.0.2; LG-V410/V41020c Build/LRX22G) ","AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 ","Chrome/34.0.1847.118 Safari/537.36","Mozilla/5.0 (CrKey armv7l 1.5.16041) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/31.0.1650.0 Safari/537.36","Mozilla/5.0 (Linux; U; Android 4.2.2; he-il; NEO-X5-116A Build/JDQ39) ","AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30","Mozilla/5.0 (Linux; Android 4.2.2; AFTB Build/JDQ39) ","AppleWebKit/537.22 (KHTML, like Gecko) ","Chrome/25.0.1364.173 Mobile Safari/537.22","Dalvik/2.1.0 (Linux; U; Android 6.0.1; Nexus Player Build/MMB29T)","AppleTV5,3/9.1.1","Mozilla/5.0 (Nintendo WiiU) ","AppleWebKit/536.30 (KHTML, like Gecko) NX/3.0.4.2.12 NintendoBrowser/4.3.1.11264.US","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Xbox; Xbox One) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/13.10586","Mozilla/5.0 (PlayStation 4 3.11) ","AppleWebKit/537.73 (KHTML, like Gecko)","Mozilla/5.0 (PlayStation Vita 3.61) ","AppleWebKit/537.73 (KHTML, like Gecko) Silk/3.2","Mozilla/5.0 (Nintendo 3DS; U; ; en) Version/1.7412.EU","Mozilla/5.0 (X11; U; Linux armv7l like Android; en-us) ","AppleWebKit/531.2+ (KHTML, like Gecko) Version/5.0 Safari/533.2+ Kindle/3.0+","Mozilla/5.0 (Linux; U; en-US) ","AppleWebKit/528.5+ (KHTML, like Gecko, Safari/528.5+) Version/4.0 Kindle/3.0 (screen 600x800; rotate)","Mozilla/5.0 (Windows NT 10.0; Win64; x64) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/42.0.2311.135 Safari/537.36 Edge/12.246","Mozilla/5.0 (X11; CrOS x86_64 8172.45.0) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/51.0.2704.64 Safari/537.36","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) ","AppleWebKit/601.3.9 (KHTML, like Gecko) Version/9.0.2 Safari/601.3.9","Mozilla/5.0 (Windows NT 6.1; WOW64) ","AppleWebKit/537.36 (KHTML, like Gecko) ","Chrome/47.0.2526.111 Safari/537.36","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1","Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)","Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)","Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"};       
	/**
	 * Connection with database
	 */
	private DBconnection db;
	
	public static void main(String[] args) {
		if(args.length != 0 && args.length != 2 && args.length != 3){
			System.err.println("Usage: java -jar crawlerPasteBin [userMongo passwordMongo] [host]");
			System.exit(0);
		}
		else if(args.length == 0){
			new Crawler().start();
		}
		else if(args.length == 2){
			new Crawler(args[0], args[1]).start();
		}
		else if(args.length == 3){
			new Crawler(args[0], args[1], args[2]).start();
		}
	}
	
	public Crawler(String user, String password, String host){
		//Initialize database connection
		db = new DBconnection("pastebin", user, password, host);
	}
	
	public Crawler(String user, String password){
		//Initialize database connection
		db = new DBconnection("pastebin", user, password);
	}
	
	public Crawler(){
		//Initialize database connection
		db = new DBconnection("pastebin");
	}
	
	/**
	 * Function to launch crawler
	 * @throws IOException 
	 */
	public void start(){
		List<String> links = new ArrayList<String>();
		links.add("");
		String lastUrl = new String();
		boolean stop = false;
		while(!stop){
			try{
				//Get listing page
				links = getNewPasteUrl(links.get(0), lastUrl);
				//Update lastUrl seen
				if(!links.isEmpty()){
					lastUrl = links.get(0);
				}
				//Crawl every post pages
				for(String url : links){
					crawlPage(url);
				}
				if(links.isEmpty()){
					links.add("");
					sleep(notFoundSleepTime);
				}
			}
			catch(Exception e){
				try {
					BufferedWriter outLog = new BufferedWriter(new FileWriter(new File("err_"+System.currentTimeMillis()+".log")));
					outLog.write(e.getMessage()+"\n"+e.getStackTrace());
					outLog.close();
				} catch (IOException e1) {
					System.out.println("Error log file can't be writed");
				}
			}
		}
	}
	
	public List<String> getNewPasteUrl(String pathList, String lastUrl){
		try {
			Document doc = getUrl(baseUrl+pathList);
			//Find list of HTML link to newer posts pages
			Elements links = doc.select("#menu_2 > ul > li > a");
			List<String> ret = new ArrayList<String>();
			for(Element link : links){
				//Stop if this page is seen yet
				if(link.attr("href").equals(lastUrl)){
					break;
				}
				else{
					ret.add(link.attr("href"));
				}
			}
			return ret;
		} catch (IOException e) {
			System.err.println("getNewPasteUrl: "+e.getMessage());
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Function to crawl post informations and save them in xml files
	 * Informations:
	 * 	Title
	 * 	Author
	 * 	Date
	 * 	Content
	 * 
	 * @param path
	 * Path to the post page
	 * 
	 */
	private void crawlPage(String path){
		try {
			Document doc = getUrl(baseUrl+path);
			Element content = doc.getElementById("paste_code");
			Elements titles = doc.getElementsByClass("paste_box_line1");
			Elements lineAuthorDate = doc.select(".paste_box_line2");
			
			String author = new String();
			String date = new String();
			if(lineAuthorDate.get(0) != null){
				String html = lineAuthorDate.get(0).html();
				//It so dirty... sorry :)
				author = html.substring(html.indexOf("\"> ")+3, html.indexOf("\n"));
				//If author has profil
				if(author.equals("")){
					author = lineAuthorDate.select("a:nth-child(2)").text();
				}
				date = lineAuthorDate.select("span").text();
			}
			
			String title = new String();
			if(titles.get(0) != null){
				title = titles.get(0).text();
			}
			
			//Save in xml file
			if(content != null){
				savePaste(title, author, date, content.text());
			}
			Set<String> emails = searchEmails(content.text());
			saveEmails(emails, title);
		} catch (IOException e) {
			System.err.println("crawlPage: "+e.getMessage());
		}
	}
	
	/**
	 * Function to save post informations in xml file
	 * 
	 * @param title
	 * @param author
	 * @param date
	 * @param content
	 */
	private void saveEmails(Set<String> emails, String pasteId){
		for(String email : emails){
			db.insertEmail(email, pasteId);
		}
	}
	
	/**
	 * Function to save post informations in xml file
	 * 
	 * @param title
	 * @param author
	 * @param date
	 * @param content
	 */
	private void savePaste(String title, String author, String date, String content){
		db.insertPaste(title, author, date, content);
	}
	
	/**
	 * Function to do dodo at thread
	 * 
	 * @param sleep
	 * Time in millis
	 */
	private void sleep(int time){
		try {
			System.out.println("Sleep: "+time+"s");
			Thread.sleep(time);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Function to do dodo at thread
	 */
	private void sleep(){
		int time = (int)((((Math.random()*60)/100)+0.7) * sleepTime);
		sleep(time);
	}
	
	/**
	 * Function to select randomly a user agent
	 * 
	 * @return User agent chosen
	 */
	private String selectUserAgent(){
        return userAgentList[(int)(Math.random() * userAgentList.length)];
    }
    
	/**
	 * Function to get cleanly a page
	 * 
	 * @param url
	 * Page Url
	 * @return Page document
	 * @throws IOException
	 */
	private Document getUrl(String url) throws IOException{
		sleep();
		String userAgent = selectUserAgent();
		System.out.println("Get "+url+"\n\t[User-agent: "+userAgent+",\n\tContent-Language: fr-FR]");
		return Jsoup.connect(url).userAgent(userAgent).header("Content-Language", "fr-FR").get();
	}

	/**
	 * Function to search emails in string
	 * @param text
	 * @return
	 */
	private Set<String> searchEmails(String text){
		Set<String> emails = new HashSet<String>();
		Pattern p = Pattern.compile(".*[^a-zA-Z0-9+_-]([a-zA-Z0-9+_-]+@[a-zA-Z0-9_.-]+\\.[a-zA-Z0-9_]+).*");
		Matcher m;
		boolean find;
		for(String s : text.split("\n")){
			m = p.matcher(text);
			find = m.matches();
			while(find){
				System.out.println(m.group(0));
				emails.add(m.group(1));
				text = text.split(m.group(1))[0];
				m = p.matcher(text);
				find = m.matches();
			}
		}
		return emails;
	}
}
