package edu.vanderbilt.vuphone.android.objects;

import java.util.ArrayList;

import android.content.Context;
import edu.vanderbilt.vuphone.android.dining.Main;
import edu.vanderbilt.vuphone.android.storage.DBAdapter;




/** 
 * @author austin
 *	This class abstracts database access and caches multiple like accesses.
 *	Accessed through the Restaurant class
 *
 * TODO: Restaurant efficient update (esp for user changing favorites)
 * 		 Restaurant delete
 * 		 caching only parts of restaurants
 * 		 reflect any new functionality in static methods in the Restaurant class
 * 
 */
public class DBWrapper {

	private static final int CLOSED = 0;
	private static final int READABLE = 1;
	private static final int WRITABLE = 2;
	private static DBAdapter adapter;
	private static int state;
	private static boolean changed = true; // set to true every time the Database is changed
	private static ArrayList<Long> IDs;
	private static ArrayList<Restaurant> cache;
	private static int cached = 0;
	// IDs and cache the same size, parallel arrays ftw
	

	@SuppressWarnings("unchecked")
	public static ArrayList<Long> getIDs() {
		if (changed || IDs==null) {
			makeReadable();
			IDs = (ArrayList<Long>)adapter.fetchAllRestaurantIDs();
			changed=false;
			// ID cache refreshed, set changed to false
			
			cache = new ArrayList<Restaurant>();
			for (int i = 0; i<IDs.size(); i++)
				cache.add(null);
			cached = 0;
			// cache now obsolete, reset with correct buffer
			
			// it is possible that cache doesnt need to be completely reset, depending on
			// the behavior of the underlying DBAdapter, but this is not sufficiently defined
			// this is safest and not extremely inefficient, 
		}
		
		return (ArrayList<Long>)IDs.clone();
	}
	
	
	public static Restaurant get(long rowID) { 
		int i = getIDs().indexOf(rowID);
		if (i==-1)
			throw new RuntimeException("Restaurant " + rowID + " not found");
		if (cache.get(i) == null) {	
			makeReadable();
			cache.set(i, adapter.fetchRestaurant(rowID));
			
			// if all restaurants are cached, close the adapter
			if (++cached == cache.size())
				close();
		}
		return cache.get(i);
	}
	
	
	
	
	// caching the entire Restaurant object is not so inefficient because 
	// it is likely the program will want the rest of the object (or part of it)
	// at some later time, and the overhead is removed then
	public static String getName(long rowID) {
		return get(rowID).getName();
	}		
	public static int getLat(long rowID) {
		return get(rowID).getLat();
	}		
	public static int getLon(long rowID) 	{
		return get(rowID).getLon();
	}
	public static RestaurantHours getHours(long rowID) {
		return get(rowID).getHours();
	}
	public static boolean favorite(long rowID) {
		return get(rowID).favorite();
	}
	
	public static boolean create(Restaurant r) {
		makeWritable();
		long rID = adapter.createRestaurant(r.getName(), r.getLat(), r.getLon(), 
				r.getDescription(), r.favorite(), r.getHours());
		changed = (rID != -1);
		return changed;
	}

	private static void initialize() {
		if (adapter == null) {
			adapter = new DBAdapter(Main.applicationContext);
			state = CLOSED;
		}
	}
	private static void makeWritable() {
		initialize();
		switch (state) {
		case READABLE:
			adapter.close();
			adapter.openWritable();
			state = WRITABLE;
			return;
		case WRITABLE:
			return;
		case CLOSED:
			adapter.openWritable();
			state = WRITABLE;
			return;
		}
	}
	private static void makeReadable() {
		initialize();
		switch (state) {
		case READABLE:
			return;
		case WRITABLE:
			adapter.close();
			adapter.openReadable();
			state = READABLE;
			return;
		case CLOSED:
			adapter.openReadable();
			state = READABLE;
			return;
		}
	}
	// closes the underlying database adapter 
	// no read/writes are to be performed in the 
	// near future
	public static void close() {
		initialize();
		switch (state) {
		case READABLE:
			adapter.close();
			state = CLOSED;
			return;
		case WRITABLE:
			adapter.close();
			state = CLOSED;
			return;
		case CLOSED:
			return;
		}
	}
}
