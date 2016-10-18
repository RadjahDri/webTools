import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class DBconnection {
	private final static String pasteTableName = "paste";
	private final static String emailTableName = "email";
	
	/**
	 * Table for paste
	 */
	private MongoCollection<Document> pasteTable;
	/**
	 * Table for email found in paste
	 */
	private MongoCollection<Document> emailTable;

	public DBconnection(String database, String user, String password, String host){
		MongoDatabase db = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+"/?authSource="+database)).getDatabase(database);
		pasteTable = db.getCollection(pasteTableName);
		emailTable = db.getCollection(emailTableName);
	}
	
	public DBconnection(String database, String user, String password){
		this(database, user, password, "127.0.0.1");
	}
	
	public DBconnection(String database){
		MongoDatabase db = new MongoClient().getDatabase(database);
		pasteTable = db.getCollection(pasteTableName);
		emailTable = db.getCollection(emailTableName);
	}
	
	public void insertEmail(String email, String pasteId){
		Document doc = new Document();
		doc.append("email", email);
		doc.append("paste", pasteId);
		
		emailTable.insertOne(doc);
	}
	
	public void insertPaste(String title, String author, String date, String content, String path){
		Document doc = new Document();
		doc.append("title", title);
		doc.append("author", author);
		doc.append("date", date);
		doc.append("content", content);
		doc.append("timestamp", System.currentTimeMillis());
		doc.append("id", path);
		
		
		pasteTable.insertOne(doc);
	}
}
