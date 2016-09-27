package io.github.kfkelvinng.nhsChoiceSearch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * Connector for Elastic Search with NhsCondition Data Access Object
 */
@Component
public class NhsEs {
	private static final Logger logger = LoggerFactory.getLogger(NhsEs.class);
	
	private static final String INDEX_NAME = "nhs";
	private static final String TYPE = "conditions";
	
	private Node node;
	private Client client;

	@PostConstruct
	public void start() throws IOException {
		Settings nodeSettings = Settings.settingsBuilder()
				.put("node.name", "nhs-singleton")
				.put("path.home", "target/es/nhs")
				.put("http.enabled", true)
				.build();

		node = NodeBuilder.nodeBuilder()
				.settings(nodeSettings)
				.data(true)
				.local(true)
				.node();
		
		client = node.client();
		
		for (String index : client.admin().indices().prepareGetIndex().get().getIndices()) {
			if (INDEX_NAME.equals(index)) {
				return;
			}
		}
		
		client
		.admin()
		.indices()
		.prepareCreate(INDEX_NAME)
		.setSource(
				jsonBuilder().startObject()
				.startObject("mappings")
					.startObject(TYPE)
						.startObject("_all")
							.field("enabled", false)
						.endObject()
						.startObject("properties")
							.startObject("url")
								.field("type", "string")
								.field("index", "not_analyzed")
							.endObject()
	
							.startObject("title")
								.field("type", "string")
								.field("index", "analyzed")
								.field("analyzer", "english")
							.endObject()
	
							.startObject("content")
								.field("type", "string")
								.field("index", "analyzed")
								.field("analyzer", "english")
							.endObject()
						.endObject()
					.endObject()
				.endObject()
				.endObject()
				)
		.get();
	}


	
	public void upsert(NhsCondition c) {
		SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
				.setTypes(TYPE)
				.setQuery(QueryBuilders.termQuery("url", c.getUrl()))
				.get();

		SearchHit[] hits = searchResponse.getHits().getHits();
		if (hits.length > 0) {
			client.prepareUpdate(INDEX_NAME, TYPE, hits[0].getId()).setDoc(new Gson().toJson(c)).get();
			return;
		}
		
		client.prepareIndex(INDEX_NAME, TYPE).setSource(new Gson().toJson(c)).get();
	}


	@PreDestroy
	public void stop() {
		node.client().close();
		node.close();
	}
}