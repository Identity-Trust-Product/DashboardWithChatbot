package com.chatbot.chatbot_fe.model;

public class DynamicDashboardModel {

	 private String key;    
	    private String type;    
	    private Object value;  
	    private String url;
	    
	    public DynamicDashboardModel() {}

	    public DynamicDashboardModel(String key, String type, Object value, String url) {
	        this.key = key;
	        this.type = type;
	        this.value = value;
	        this.url = url;
	    }
	   
	    public String getKey() {
	        return key;
	    }

	    public void setKey(String key) {
	        this.key = key;
	    }

	    public String getType() {
	        return type;
	    }

	    public void setType(String type) {
	        this.type = type;
	    }

	    public Object getValue() {
	        return value;
	    }

	    public void setValue(Object value) {
	        this.value = value;
	    }

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "DynamicDashboardModel [key=" + key + ", type=" + type + ", value=" + value + ", url=" + url + "]";
		}
}
