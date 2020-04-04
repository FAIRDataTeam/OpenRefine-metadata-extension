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
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class Vocabulary {

    private static final SimpleValueFactory vf = SimpleValueFactory.getInstance();
    // "Shared"
    public static final IRI TYPE = RDF.TYPE;
    public static final IRI TYPE_CATALOG = DCAT.CATALOG;
    public static final IRI TYPE_DATASET = DCAT.DATASET;
    public static final IRI TYPE_DISTRIBUTION = DCAT.DISTRIBUTION;
    public static final IRI TITLE = vf.createIRI("http://purl.org/dc/terms/title");
    public static final IRI VERSION = vf.createIRI("http://purl.org/dc/terms/hasVersion");
    public static final IRI DESCRIPTION = vf.createIRI("http://purl.org/dc/terms/description");
    public static final IRI LICENSE = vf.createIRI("http://purl.org/dc/terms/license");
    public static final IRI RIGHTS = vf.createIRI("http://purl.org/dc/terms/accessRights");
    public static final IRI PUBLISHER = vf.createIRI("http://purl.org/dc/terms/publisher");
    public static final IRI PUBLISHER_NAME = vf.createIRI("http://xmlns.com/foaf/0.1/name");
    public static final IRI PARENT = vf.createIRI("http://purl.org/dc/terms/isPartOf");
    public static final IRI LANGUAGE = vf.createIRI("http://purl.org/dc/terms/language");
    // "Repository"
    public static final IRI CATALOG = vf.createIRI("http://www.re3data.org/schema/3-0#dataCatalog");
    // "Catalog"
    public static final IRI DATASET = vf.createIRI("http://www.w3.org/ns/dcat#dataset");
    public static final IRI HOMEPAGE = vf.createIRI("http://xmlns.com/foaf/0.1/homepage");
    public static final IRI THEME_TAXONOMY = vf.createIRI("https://www.w3.org/ns/dcat#themeTaxonomy");
    // "Dataset"
    public static final IRI DISTRIBUTION = vf.createIRI("http://www.w3.org/ns/dcat#distribution");
    public static final IRI CONTACT_POINT = vf.createIRI("http://www.w3.org/ns/dcat#contactPoint");
    public static final IRI LANDING_PAGE = vf.createIRI("http://www.w3.org/ns/dcat#landingPage");
    public static final IRI THEME = vf.createIRI("http://www.w3.org/ns/dcat#theme");
    public static final IRI KEYWORD = vf.createIRI("http://www.w3.org/ns/dcat#keyword");
    // "Distribution"
    public static final IRI FORMAT = vf.createIRI("http://purl.org/dc/terms/format");
    public static final IRI BYTE_SIZE = vf.createIRI("http://www.w3.org/ns/dcat#byteSize");
    public static final IRI MEDIA_TYPE = vf.createIRI("http://www.w3.org/ns/dcat#mediaType");
    public static final IRI DOWNLOAD_URL = vf.createIRI("http://www.w3.org/ns/dcat#downloadUrl");
    public static final IRI ACCESS_URL = vf.createIRI("http://www.w3.org/ns/dcat#accessUrl");

}
