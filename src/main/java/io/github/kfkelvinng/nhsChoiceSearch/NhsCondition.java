package io.github.kfkelvinng.nhsChoiceSearch;

public class NhsCondition {
	
	public static class Builder {
		String url;
		String title;
		String content;
		
		public Builder setUrl(String url) {
			this.url = url;
			return this;
		}
		
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}
		
		public Builder setContent(String content) {
			this.content = content;
			return this;
		}
		
		public NhsCondition build() {
			return new NhsCondition(url, title, content);
		}
	}
	

	private String url;
	private String title;
	private String content;
	
	private NhsCondition(String url, String title, String content) {
		this.url = url;
		this.title = title;
		this.content = content;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getContent() {
		return content;
	}

}
