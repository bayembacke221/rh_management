package sn.bmbacke.rh.service;

import java.util.Map; /**
 * Interface du service de génération de documents
 */
public interface DocumentGenerationService {

    /**
     * Génère un document PDF à partir d'un template et des données
     */
    byte[] generatePDF(String templateName, Map<String, Object> data);

    /**
     * Génère un document Excel à partir des données
     */
    byte[] generateExcel(String sheetName, Object data);

    /**
     * Génère un document CSV à partir des données
     */
    byte[] generateCSV(Object data);
}
