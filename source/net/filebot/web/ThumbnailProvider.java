package net.filebot.web;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

public interface ThumbnailProvider {

	Map<SearchResult, Icon> getThumbnails(List<SearchResult> keys) throws Exception;

}
