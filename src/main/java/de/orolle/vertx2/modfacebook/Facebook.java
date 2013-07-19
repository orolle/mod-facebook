package de.orolle.vertx2.modfacebook;

import java.util.HashMap;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * This Vertx Module allows to access Facebooks FQL API
 * 
 * @author Oliver Rolle
 *
 */
public class Facebook extends BusModBase{
	public String address = null;
	public int http_timeout = -1;
	
	@Override
	public void start() {
		super.start();
		
		this.address = this.config.getString("address", "de.orolle.vertx2.modfacebook");
		this.http_timeout = this.config.getInteger("http-timeout", 10000);
		
		Handler<Message<JsonObject>> incoming = new Handler<Message<JsonObject>>() {
			
			@Override
			public void handle(final Message<JsonObject> msg) {
				final String action = msg.body().getString("action","");
				
				switch (action) {
				case "fql":
					final JsonObject query = msg.body().getObject("queries", new JsonObject());
					fql(query, msg.body().getString("accesstoken", ""), new Handler<JsonObject>(){
						@Override
						public void handle(JsonObject event) {
							msg.reply(event);
						}
						
					});
					break;

				default:
					break;
				}
			}
		};
		
		this.eb.registerHandler(this.address, incoming);
	}

	/**
	 * Requests bundled queries in a single HTTPS request.
	 * 
	 * @param query
	 * 	FQL Queries śaved in a JsonObject {"query1": "select * ...", "query2": "..."}
	 * @param accessToken
	 * 	Facebook access token. Get test access token on https://developers.facebook.com/tools/explorer or use OAuth login.
	 * @param handler
	 * 	Receive query result
	 */
	public void fql(final JsonObject query, final String accessToken, final Handler<JsonObject> handler) {		
		getFQL(vertx.createHttpClient(), query, accessToken, new Handler<JsonObject>() {
			@Override
			public void handle(JsonObject o) {
				handler.handle(o);
			}
		});
	}

	
	public void getFQL(final HttpClient httpClient, final JsonObject query, final String accessToken, final Handler<JsonObject> handler) {		
		final String url = "/fql?q="+URIHelper.uri(query.toString())+"&method=GET&format=json&access_token="+accessToken;
				
		HttpClientRequest fqlRequest = httpClient.setConnectTimeout(this.http_timeout).setSSL(true).setPort(443).setHost("graph.facebook.com")
				.get(url, new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse event) {
						event.bodyHandler(new Handler<Buffer>() {
							
							public void handle(Buffer event) {
								String str = event.toString();
								JsonObject response = null;

								try{
									response = new JsonObject(str);
								}
								catch (Exception e) {
									try{
										response = new JsonObject("{\""+str+"\"}");
									}catch (Exception e1) {
									}
								}

								handler.handle(simplifyFQLResult(response));
							}
						});

						event.exceptionHandler(new Handler<Throwable>() {
							@Override
							public void handle(Throwable event) {
								event.printStackTrace();

								handler.handle(new JsonObject().putString("error", "query execution failed because of exception"));
							}
						});
					}
				});

		fqlRequest.exceptionHandler(new Handler<Throwable>() {
			@Override
			public void handle(Throwable event) {
				event.printStackTrace();
				handler.handle(new JsonObject().putString("error", "query execution failed because of exception"));
			}
		});

		fqlRequest.end();
	}
	
	@SuppressWarnings("unchecked")
	private static JsonObject simplifyFQLResult(JsonObject fqlResultSet) {
//		System.out.println("FQL-RESULT: "+fqlResultSet.toString());
		
		if(fqlResultSet.getObject("error") != null){
			return fqlResultSet;
		}
		
		Object[] objs = fqlResultSet.getArray("data").toArray();
		JsonObject result = new JsonObject();

		for (Object container : objs) {
			JsonObject jContainer = new JsonObject((HashMap<String, Object>) container);
			final String queryName = jContainer.getString("name");

			if(queryName!=null){
				Object obj = jContainer.getField("fql_result_set");
				JsonArray subResult = null;

				if (obj instanceof Object[]) {
					Object[] os = (Object[]) obj;
					subResult = new JsonArray(os);
				}else if (obj instanceof JsonArray) {
					JsonArray as = (JsonArray) obj;
					subResult = as;
				}

				if(subResult != null){
					result.putArray(queryName, subResult);
				}
			}
		}

		return result;
	}
}
