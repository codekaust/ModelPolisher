package edu.ucsd.sbrg.bigg;

import java.io.File;

import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBProperties;

/**
 * Helper class to store all parameters for running ModelPolisher in batch
 * mode.
 *
 * @author Thomas Zajac
 */
class ModelPolisherParameters {

  /**
   * Singleton for
   */
  private static ModelPolisherParameters parameters = null;
  /**
   * @see ModelPolisherOptions#INCLUDE_ANY_URI
   */
  Boolean includeAnyURI = null;
  /**
   * @see ModelPolisherOptions#ANNOTATE_WITH_BIGG
   */
  Boolean annotateWithBiGG = null;
  /**
   * @see ModelPolisherOptions#OUTPUT_COMBINE
   */
  Boolean outputCOMBINE = null;
  /**
   * @see ModelPolisherOptions#ADD_ADB_ANNOTATIONS
   */
  Boolean addADAAnnotations = null;
  /**
   * @see ModelPolisherOptions#CHECK_MASS_BALANCE
   */
  Boolean checkMassBalance = null;
  /**
   * @see ModelPolisherOptions#NO_MODEL_NOTES
   */
  Boolean noModelNotes = null;
  /**
   * @see ModelPolisherOptions#COMPRESSION_TYPE
   */
  ModelPolisherOptions.Compression compression = ModelPolisherOptions.Compression.NONE;
  /**
   * Can be {@code null}
   *
   * @see ModelPolisherOptions#DOCUMENT_NOTES_FILE
   */
  File documentNotesFile = null;
  /**
   * Can be {@code null} (then a default is used).
   *
   * @see ModelPolisherOptions#DOCUMENT_TITLE_PATTERN
   */
  String documentTitlePattern = null;
  /**
   * @see ModelPolisherOptions#FLUX_COEFFICIENTS
   */
  double[] fluxCoefficients = null;
  /**
   * @see ModelPolisherOptions#FLUX_OBJECTIVES
   */
  String[] fluxObjectives = null;
  /**
   * Can be {@code null}
   *
   * @see ModelPolisherOptions#MODEL_NOTES_FILE
   */
  File modelNotesFile = null;
  /**
   * @see ModelPolisherOptions#OMIT_GENERIC_TERMS
   */
  Boolean omitGenericTerms = null;
  /**
   * @see ModelPolisherOptions#SBML_VALIDATION
   */
  Boolean sbmlValidation = null;

  private ModelPolisherParameters(SBProperties args) {
    String documentTitlePattern = null;
    if (args.containsKey(ModelPolisherOptions.DOCUMENT_TITLE_PATTERN)) {
      documentTitlePattern = args.getProperty(ModelPolisherOptions.DOCUMENT_TITLE_PATTERN);
    }
    double[] coefficients = null;
    if (args.containsKey(ModelPolisherOptions.FLUX_COEFFICIENTS)) {
      String c = args.getProperty(ModelPolisherOptions.FLUX_COEFFICIENTS);
      String[] coeff = c.substring(1, c.length() - 1).split(",");
      coefficients = new double[coeff.length];
      for (int i = 0; i < coeff.length; i++) {
        coefficients[i] = Double.parseDouble(coeff[i].trim());
      }
    }
    String[] fObj = null;
    if (args.containsKey(ModelPolisherOptions.FLUX_OBJECTIVES)) {
      String fObjectives = args.getProperty(ModelPolisherOptions.FLUX_OBJECTIVES);
      fObj = fObjectives.substring(1, fObjectives.length() - 1).split(":");
    }
    parameters.annotateWithBiGG = args.getBooleanProperty(ModelPolisherOptions.ANNOTATE_WITH_BIGG);
    parameters.outputCOMBINE = args.getBooleanProperty(ModelPolisherOptions.OUTPUT_COMBINE);
    parameters.addADAAnnotations = args.getBooleanProperty(ModelPolisherOptions.ADD_ADB_ANNOTATIONS);
    parameters.checkMassBalance = args.getBooleanProperty(ModelPolisherOptions.CHECK_MASS_BALANCE);
    parameters.noModelNotes = args.getBooleanProperty(ModelPolisherOptions.NO_MODEL_NOTES);
    parameters.compression =
      ModelPolisherOptions.Compression.valueOf(args.getProperty(ModelPolisherOptions.COMPRESSION_TYPE));
    parameters.documentNotesFile = parseFileOption(args, ModelPolisherOptions.DOCUMENT_NOTES_FILE);
    parameters.documentTitlePattern = documentTitlePattern;
    parameters.fluxCoefficients = coefficients;
    parameters.fluxObjectives = fObj;
    parameters.includeAnyURI = args.getBooleanProperty(ModelPolisherOptions.INCLUDE_ANY_URI);
    parameters.modelNotesFile = parseFileOption(args, ModelPolisherOptions.MODEL_NOTES_FILE);
    parameters.omitGenericTerms = args.getBooleanProperty(ModelPolisherOptions.OMIT_GENERIC_TERMS);
    parameters.sbmlValidation = args.getBooleanProperty(ModelPolisherOptions.SBML_VALIDATION);
  }


  /**
   * Scans the given command-line options for a specific file option and
   * returns the corresponding file if it exists, {@code null} otherwise.
   *
   * @param args
   *        command-line options.
   * @param option
   *        a specific file option to look for.
   * @return a {@link File} object that corresponds to a desired command-line
   *         option, or {@code null} if it does not exist.
   */
  private File parseFileOption(SBProperties args, Option<File> option) {
    if (args.containsKey(option)) {
      File notesFile = new File(args.getProperty(option));
      if (notesFile.exists() && notesFile.canRead()) {
        return notesFile;
      }
    }
    return null;
  }


  /**
   * @param args
   *        SBProperties object containing all options
   * @return
   *         Handle to initialized and set parameters
   */
  static ModelPolisherParameters initParameters(SBProperties args) {
    if (parameters == null) {
      parameters = new ModelPolisherParameters(args);
    }
    return parameters;
  }
}
