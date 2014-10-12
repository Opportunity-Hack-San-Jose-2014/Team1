/**
 * UserProfile class
 */
package com.ebay.ohack2014;



/**
 * @author vinagar
 *
 */

public class KeyValue {
	String key;
	String value;
 
	public String getkey() {
		return key;
	}
 
	public void setkey(String key) {
		this.key = key;
	}
 
	public String getvalue() {
		return value;
	}
 
	public void setvalue(String value) {
		this.value = value;
	}
 
	@Override
	public String toString() {
		return "Track [key=" + key + ", value=" + value + "]";
	}
}
