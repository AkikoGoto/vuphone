 /**************************************************************************
 * Copyright 2009 Chris Thompson                                           *
 *                                                                         *
 * Licensed under the Apache License, Version 2.0 (the "License");         *
 * you may not use this file except in compliance with the License.        *
 * You may obtain a copy of the License at                                 *
 *                                                                         *
 * http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                         *
 * Unless required by applicable law or agreed to in writing, software     *
 * distributed under the License is distributed on an "AS IS" BASIS,       *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*
 * See the License for the specific language governing permissions and     *
 * limitations under the License.                                          *
 **************************************************************************/
package org.vuphone.vandyupon.notification.ratingpost.event;

import org.vuphone.vandyupon.notification.Notification;

public class EventRatingPost extends Notification {
	
	private long userId_;
	private String comment_;
	private String value_;
	
	public EventRatingPost(long user, String comment, String value){
		super("eventratingpost");
		userId_ = user;
		comment_ = comment;
		value_ = value;
	}
	
	public String getComment(){
		return comment_;
	}
	
	public String getValue(){
		return value_;
	}
	
	public long getUser(){
		return userId_;
	}

}