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
package solutions.fairdata.openrefine.metadata.fdp.transformers;

import nl.dtl.fairmetadata4j.model.Agent;
import nl.dtl.fairmetadata4j.model.Identifier;
import nl.dtl.fairmetadata4j.model.Metadata;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import solutions.fairdata.openrefine.metadata.dto.metadata.MetadataDTO;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataTransformerUtils {

    private static SimpleValueFactory literalFactory = SimpleValueFactory.getInstance();

    public static String iriToString(IRI iri) {
        return iri == null ? null : iri.toString();
    }

    public static IRI stringToIri(String iri) {
        return iri == null ? null : literalFactory.createIRI(iri);
    }

    public static String literalToString(Literal literal) {
        return literal == null ? null : literal.getLabel();
    }

    public static Literal stringToLiteral(String value) {
        return value == null ? null : literalFactory.createLiteral(value);
    }

    public static List<String> irisToStrings(List<IRI> iris) {
        return iris == null ? null : iris.stream().map(MetadataTransformerUtils::iriToString).collect(Collectors.toList());
    }

    public static List<IRI> stringsToIris(List<String> strings) {
        return strings == null ? null : strings.stream().map(MetadataTransformerUtils::stringToIri).collect(Collectors.toList());
    }

    public static List<String> literalsToStrings(List<Literal> literals) {
        return literals == null ? null : literals.stream().map(MetadataTransformerUtils::literalToString).collect(Collectors.toList());
    }

    public static List<Literal> stringsToLiterals(List<String> strings) {
        return strings == null ? null : strings.stream().map(MetadataTransformerUtils::stringToLiteral).collect(Collectors.toList());
    }

    public static Agent createAgent(String iri, String name) {
        if (iri == null) return null;
        Agent agent = new Agent();
        agent.setUri(stringToIri(iri));
        agent.setName(stringToLiteral(name));
        return agent;
    }

    public static void genericDtoFromMetadata(MetadataDTO dto, Metadata metadata) {
        dto.setIri(iriToString(metadata.getUri()));
        dto.setTitle(literalToString(metadata.getTitle()));
        dto.setVersion(literalToString(metadata.getVersion()));
        dto.setLicense(iriToString(metadata.getLicense()));
        dto.setDescription(literalToString(metadata.getDescription()));
        dto.setRights(iriToString(metadata.getRights()));

        dto.setPublisher(iriToString(metadata.getPublisher().getUri()));
        dto.setPublisherName(literalToString(metadata.getPublisher().getName()));
    }

    public static void genericMetadataFromDto(Metadata metadata, MetadataDTO dto) {
        metadata.setUri(stringToIri(dto.getIri()));
        metadata.setTitle(stringToLiteral(dto.getTitle()));
        metadata.setVersion(stringToLiteral(dto.getVersion()));
        metadata.setLicense(stringToIri(dto.getLicense()));
        metadata.setDescription(stringToLiteral(dto.getDescription()));
        metadata.setRights(stringToIri(dto.getRights()));

        metadata.setPublisher(createAgent(dto.getPublisher(), dto.getPublisherName()));
    }

    public static Identifier createIdentifier(String uri) {
        Identifier identifier = new Identifier();
        identifier.setIdentifier(stringToLiteral(uri));
        identifier.setUri(stringToIri(uri));
        return identifier;
    }

    public static void setTimestamps(Metadata metadata) {
        setTimestamps(metadata, new Date());
    }

    public static void setTimestamps(Metadata metadata, Date date) {
        metadata.setModified(literalFactory.createLiteral(date));
        metadata.setIssued(literalFactory.createLiteral(date));
    }
}
