package edu.ucsd.sbrg.bigg;

import static org.junit.Assert.assertTrue;

import edu.ucsd.sbrg.miriam.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.GeneProduct;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BiGGAnnotationTest {

  private static SBMLDocument doc;
  private static Map<String, String> scrambledSpeciesIds = new HashMap<>();
  private static Map<String, String> scrambledReactionIds = new HashMap<>();


  @BeforeClass
  public static void setUp() {
    // load annotated files for scrambling
    URL resourcePath = BiGGAnnotationTest.class.getClassLoader().getResource("resources");
    File resources = new File(resourcePath.getFile());
    File[] files = resources.listFiles();
    if (files == null) {
      System.out.println("No files to scramble, put SBML models into test/resources");
      System.exit(1);
    }
    File file = files[0];
    System.out.println(String.format("Scrambling annotated ids in file %s", file.getAbsolutePath()));
    processFile(file);
    scrambledSpeciesIds = invertMap(scrambledSpeciesIds);
    scrambledReactionIds = invertMap(scrambledReactionIds);
  }


  /**
   * @param file
   */
  private static void processFile(File file) {
    SBMLReader reader = new SBMLReader();
    try {
      doc = reader.readSBML(file);
    } catch (XMLStreamException | IOException e) {
      e.printStackTrace();
    }
    // scramble bigg_ids
    Model model = doc.getModel();
    scrambleSpeciesList(model.getListOfSpecies());
    scrambleReactionList(model.getListOfReactions());
    scrambleGPRList(model);
    doc = model.getSBMLDocument();
  }


  /**
   * @param speciesList
   */
  private static void scrambleSpeciesList(ListOf<Species> speciesList) {
    for (Species species : speciesList) {
      scrambleSpecies(species);
    }
  }


  /**
   * @param species
   */
  private static void scrambleSpecies(Species species) {
    if (hasMiriamAnnotation(species.getAnnotation())) {
      String id = species.getId();
      boolean isBiGGid = id.matches("^([RMG])_([a-zA-Z][a-zA-Z0-9_]+)(?:_([a-z][a-z0-9]?))?(?:_([A-Z][A-Z0-9]?))?$");
      if (isBiGGid) {
        String newId = "iiiii" + id;
        species.setId(newId);
        scrambledSpeciesIds.put(id, newId);
      }
    }
  }


  /**
   * @param annotation
   * @return
   */
  private static boolean hasMiriamAnnotation(Annotation annotation) {
    for (CVTerm cvTerm : annotation.getListOfCVTerms()) {
      for (int i = 0; i < cvTerm.getNumResources(); i++) {
        String resource = cvTerm.getResource(i);
        if (!Registry.getDataCollectionPartFromURI(resource).equals("")) {
          return true;
        }
      }
    }
    return false;
  }


  /**
   * @param reactionsList
   */
  private static void scrambleReactionList(ListOf<Reaction> reactionsList) {
    for (Reaction reaction : reactionsList) {
      scrambleReaction(reaction);
    }
  }


  /**
   * @param reaction
   */
  private static void scrambleReaction(Reaction reaction) {
    String id = reaction.getId();
    String newId = "iiiii" + id;
    reaction.setId(newId);
    scrambledReactionIds.put(id, newId);
    for (SpeciesReference speciesReference : reaction.getListOfReactants()) {
      scrambleSpeciesReference(speciesReference);
    }
    for (SpeciesReference speciesReference : reaction.getListOfProducts()) {
      scrambleSpeciesReference(speciesReference);
    }
  }


  /**
   * @param speciesReference
   */
  private static void scrambleSpeciesReference(SpeciesReference speciesReference) {
    String speciesId = speciesReference.getSpecies();
    String scrambledId = scrambledSpeciesIds.getOrDefault(speciesId, "");
    if (!scrambledId.equals("")) {
      speciesReference.setSpecies(scrambledId);
    }
  }


  /**
   * @param model
   */
  private static void scrambleGPRList(Model model) {
    FBCModelPlugin fbcPlugin = (FBCModelPlugin) model.getPlugin(FBCConstants.shortLabel);
    for (int i = 0; i < fbcPlugin.getGeneProductCount(); i++) {
      scrambleGPR(fbcPlugin.getGeneProduct(i));
    }
  }


  /**
   * @param map
   * @return
   */
  private static Map<String, String> invertMap(Map<String, String> map) {
    // assume unique values
    Map<String, String> inverted = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      inverted.put(entry.getValue(), entry.getKey());
    }
    return inverted;
  }


  /**
   * @param gpr
   */
  private static void scrambleGPR(GeneProduct gpr) {
    // TODO
  }


  @Test
  public final void testSpeciesRestoreBiGGId() {
    // TODO: insert proper test
    assertTrue(true);
  }


  @Test
  public final void testReactionRestoreBiGGId() {
    // TODO: insert proper test
    assertTrue(true);
  }


  @Test
  public final void testGPRRestoreBiGGId() {
    // TODO: insert proper test
    assertTrue(true);
  }
}
