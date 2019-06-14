package edu.ucsd.sbrg.miriam;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.BeforeClass;

import java.util.Optional;

public class RegistryTest {

  @Test
  public final void getPrimaryURITest() {
    String[] URIs = new String[] {"http://identifiers.org/taxonomy/536056", "http://www.taxonomy.org/536056",
      "http://www.uniprot.org/taxonomy/536056"};
    String target = URIs[0].substring(0, URIs[0].lastIndexOf("/") + 1);
    for (String URI : URIs) {
      Optional<String> primary = Registry.getPrimaryURI(URI);
      primary.ifPresent(p -> assertEquals(p, target));
    }
  }


  @Test
  public final void getDataCollectionPartFromURITest() {
    String URI = "http://identifiers.org/bigg.metabolite/13dpg";
    assertEquals(Registry.getDataCollectionPartFromURI(URI), "bigg.metabolite");
  }


  @Test
  public final void getCollectionForTest() {
    String URI = "http://www.reactome.org/content/detail/R-ALL-964859";
    Optional<String> collection = Registry.getCollectionFor(URI);
    assertEquals(collection.orElse(""), "reactome");
  }
}
