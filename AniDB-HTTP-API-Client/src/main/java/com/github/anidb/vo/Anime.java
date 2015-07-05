package com.github.anidb.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Anime {

	@XmlAttribute
	private int id;
	private String type;
	private int episodecount;

	private String picture;

	private String description;
	private Ratings ratings = new Ratings();
	@XmlElementWrapper(name = "titles")
	@XmlElement(name = "title")
	private List<Title> titles = new ArrayList<>();
	@XmlElementWrapper(name = "categories")
	@XmlElement(name = "categorie")
	private List<Categorie> categories = new ArrayList<>();
	@XmlElementWrapper(name = "tags")
	@XmlElement(name = "tag")
	private List<Tag> tags = new ArrayList<>();
	@XmlElementWrapper(name = "episodes")
	@XmlElement(name = "episode")
	private List<Episode> episodes = new ArrayList<>();

	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}

	public String getType() {
		return type;
	}

	public void setTitles(List<Title> titles) {
		this.titles = titles;
	}

	public void setRatings(Ratings ratings) {
		this.ratings = ratings;
	}

	public void setCategories(List<Categorie> categories) {
		this.categories = categories;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getEpisodecount() {
		return episodecount;
	}

	public void setEpisodecount(int episodecount) {
		this.episodecount = episodecount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Title> getTitles() {
		return titles;
	}

	public Ratings getRatings() {
		return ratings;
	}

	public List<Categorie> getCategories() {
		return categories;
	}

}
