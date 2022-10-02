package com.github.anidb.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Ratings {
	private double permanent;
	private double temporary;
	private double review;

	public double getPermanent() {
		return permanent;
	}

	public void setPermanent(double permanent) {
		this.permanent = permanent;
	}

	public double getTemporary() {
		return temporary;
	}

	public void setTemporary(double temporary) {
		this.temporary = temporary;
	}

	public double getReview() {
		return review;
	}

	public void setReview(double review) {
		this.review = review;
	}
}
