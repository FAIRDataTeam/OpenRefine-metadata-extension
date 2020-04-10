/**
 * The MIT License
 * Copyright © 2019 FAIR Data Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.fairdata.openrefine.metadata.fdp;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class VocabularyHelper {

    private static final SimpleValueFactory vf = SimpleValueFactory.getInstance();
    // "Shared"
    public static final IRI TYPE = RDF.TYPE;
    public static final IRI TYPE_CATALOG = DCAT.CATALOG;
    public static final IRI TYPE_DATASET = DCAT.DATASET;
    public static final IRI TYPE_DISTRIBUTION = DCAT.DISTRIBUTION;
    public static final IRI TITLE = DCTERMS.TITLE;
    public static final IRI VERSION = DCTERMS.HAS_VERSION;
    public static final IRI DESCRIPTION = DCTERMS.DESCRIPTION;
    public static final IRI LICENSE = DCTERMS.LICENSE;
    public static final IRI RIGHTS = DCTERMS.RIGHTS;
    public static final IRI PUBLISHER = DCTERMS.PUBLISHER;
    public static final IRI PUBLISHER_NAME = FOAF.NAME;
    public static final IRI PARENT = DCTERMS.IS_PART_OF;
    public static final IRI LANGUAGE = DCTERMS.LANGUAGE;
    // "Repository"
    public static final IRI CATALOG = vf.createIRI("http://www.re3data.org/schema/3-0#dataCatalog");
    // "Catalog"
    public static final IRI DATASET = DCAT.DATASET;
    public static final IRI HOMEPAGE = FOAF.HOMEPAGE;
    public static final IRI THEME_TAXONOMY = DCAT.THEME_TAXONOMY;
    // "Dataset"
    public static final IRI DISTRIBUTION = DCAT.DISTRIBUTION;
    public static final IRI CONTACT_POINT = DCAT.CONTACT_POINT;
    public static final IRI LANDING_PAGE = DCAT.LANDING_PAGE;
    public static final IRI THEME = DCAT.THEME;
    public static final IRI KEYWORD = DCAT.KEYWORD;
    // "Distribution"
    public static final IRI FORMAT = DCTERMS.FORMAT;
    public static final IRI BYTE_SIZE = DCAT.BYTE_SIZE;
    public static final IRI MEDIA_TYPE = DCAT.MEDIA_TYPE;
    public static final IRI DOWNLOAD_URL = DCAT.DOWNLOAD_URL;
    public static final IRI ACCESS_URL = DCAT.ACCESS_URL;

}
