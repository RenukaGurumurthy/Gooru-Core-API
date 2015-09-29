/**
 * 
 */
package org.ednovo.gooru.cassandra.core;


/**
 * @author SearchTeam
 * 
 */
public interface IsIndexSrcBuilder<I, O> {

	O build(I input, Boolean fetchFromSlave);
	
	String getName();
}
