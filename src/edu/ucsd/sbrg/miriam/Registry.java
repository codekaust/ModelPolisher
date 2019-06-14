package edu.ucsd.sbrg.miriam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import edu.ucsd.sbrg.miriam.xjc.Miriam;
import edu.ucsd.sbrg.miriam.xjc.Uri;
import edu.ucsd.sbrg.miriam.xjc.Uris;

public class Registry {

  /**
   * A {@link Logger} for this class.
   */
  static final transient Logger logger = Logger.getLogger(Registry.class.getName());
  /**
   * Stores primary URI to regex pattern mapping
   */
  private static HashMap<String, Entry> entries = new HashMap<>();

  private static class Entry {

    private List<String> alternativeURIs = new ArrayList<>();
    private String primaryURI;
    private String regexPattern;
    private HashMap<String, Boolean> URIs;


    Entry(HashMap<String, Boolean> URIs, String regexPattern) {
      this.URIs = URIs;
      this.regexPattern = regexPattern;
      extractPrimary();
    }


    private void extractPrimary() {
      for (Map.Entry<String, Boolean> URI : URIs.entrySet()) {
        if (Pattern.matches("http://identifiers\\.org/.*", URI.getKey())) {
          primaryURI = URI.getKey();
          break;
        }
      }
      Set<String> remainingURIs = URIs.keySet();
      remainingURIs.remove(primaryURI);
      alternativeURIs.addAll(remainingURIs);
    }


    String getPrimaryURI() {
      if (primaryURI == null) {
        return alternativeURIs.get(0);
      } else {
        return primaryURI;
      }
    }


    Optional<String> getPrimaryURI(String query) {
      // only handle URLs for now
      if (!query.contains("/")) {
        logger.warning(String.format("Not a URL, can't get primary URI for %s", query));
        return Optional.empty();
      }
      query = getCollectionPrefix(query);
      if (query.equals(primaryURI)) {
        return Optional.of(primaryURI);
      }
      for (String URI : alternativeURIs) {
        if (URI.equals(query)) {
          return Optional.of(primaryURI);
        }
      }
      return Optional.empty();
    }


    String getPattern() {
      return regexPattern;
    }


    boolean checkPattern(String query) {
      return Pattern.matches(regexPattern, query);
    }
  }

  // only extract necessary information and use a static initializer to fill entries
  static {
    Miriam miriam = RegistryProvider.getInstance().getMiriam();
    for (Miriam.Datatype datatype : miriam.getDatatype()) {
      String pattern = datatype.getPattern();
      HashMap<String, Boolean> allURIs = new HashMap<>();
      // ugly xjc generated Classes, a custom wrapper might be better here
      for (Uris uris : datatype.getUris()) {
        for (Uri uri : uris.getUri()) {
          allURIs.put(uri.getValue(), uri.isDeprecated());
        }
      }
      Entry entry = new Entry(allURIs, pattern);
      entries.put(getDataCollectionPartFromURI(entry.getPrimaryURI()), entry);
    }
    RegistryProvider.close();
  }


  /**
   * @param query:
   *        Identifier part of identifiers.org URI
   * @param collection:
   *        Collection part of identifiers.org URI
   * @return
   */
  public static Boolean checkPattern(String query, String collection) {
    if (entries.containsKey(collection)) {
      return entries.get(collection).checkPattern(query);
    } else {
      return false;
    }
  }


  /**
   * Get pattern from collection name
   * 
   * @param collection
   * @return
   */
  public static String getPattern(String collection) {
    if (entries.containsKey(collection)) {
      return entries.get(collection).getPattern();
    } else {
      return "";
    }
  }


  /**
   * @param queryURI:
   *        Non identifiers.org URI
   * @return identifiers.org URI, if found, else empty
   */
  public static Optional<String> getCollectionFor(String queryURI) {
    Optional<String> primaryURI = getPrimaryURI(queryURI);
    return primaryURI.map(Registry::getDataCollectionPartFromURI);
  }


  /**
   * Get collection from identifiers.org URI
   * 
   * @param resource:
   *        identifiers.org URI
   * @return
   */
  public static String getDataCollectionPartFromURI(String resource) {
    if (resource.contains("identifiers.org")) {
      return resource.split("/")[3];
    } else {
      return resource.split("/")[2];
    }
  }


  /**
   * Get identifier from identifiers.org URI
   *
   * @param resource:
   *        Full identifiers.org URI
   * @return
   */
  public static String getIdentifierFromURI(String resource) {
    String identifiersURL = "identifiers.org";
    if (resource.contains(identifiersURL)) {
      // We know where the id should be in identifiers.org URLs
      resource = resource.substring(resource.indexOf(identifiersURL) + identifiersURL.length() + 1);
      return resource.substring(resource.indexOf("/") + 1);
    } else {
      // assume last part after slash is ID
      String[] split = resource.split("/");
      int len = split.length;
      return split[len - 1];
    }
  }


  /**
   * @param queryURI:
   *        Non identifiers.org URI
   * @return identifiers.org URI, if found, else empty
   */
  public static Optional<String> getPrimaryURI(String queryURI) {
    for (Map.Entry<String, Entry> entry : entries.entrySet()) {
      Optional<String> collection = entry.getValue().getPrimaryURI(queryURI);
      if (collection.isPresent()) {
        return collection;
      }
    }
    return Optional.empty();
  }


  /**
   * @param queryURI
   * @return
   */
  public static String getCollectionPrefix(String queryURI) {
    String[] splits = queryURI.split("/");
    int length = splits.length;
    String prefix = String.join("/", splits[0], splits[1], splits[2]);
    if (queryURI.contains("identifiers.org") || queryURI.contains("uniprot.org") && length > 3) {
      prefix = String.join("/", prefix, splits[3]);
    }
    return prefix + "/";
  }


  /**
   * @param resource
   * @param pattern
   * @param replacement
   * @return
   */
  public static String replace(String resource, String pattern, String replacement) {
    return resource.replaceAll(pattern, replacement);
  }


  /**
   * Builds full identifiers.org URI from collection and identifier
   * 
   * @param collection:
   *        Collection part of identifiers.org URI
   * @param id:
   *        Identifier part of identifiers.org URI
   * @return
   */
  public static String getURI(String collection, String id) {
    String base = "http://identifiers.org/";
    return base + collection + "/" + id;
  }
}
