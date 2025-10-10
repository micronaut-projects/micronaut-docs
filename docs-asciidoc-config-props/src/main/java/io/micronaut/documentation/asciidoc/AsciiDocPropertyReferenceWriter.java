/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.documentation.asciidoc;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ast.Element;
import io.micronaut.inject.configuration.ConfigurationMetadata;
import io.micronaut.inject.configuration.ConfigurationMetadataBuilder;
import io.micronaut.inject.configuration.ConfigurationMetadataWriter;
import io.micronaut.inject.configuration.PropertyMetadata;
import io.micronaut.inject.writer.ClassWriterOutputVisitor;
import io.micronaut.inject.writer.GeneratedFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Writes out the asciidoc configuration property reference.
 *
 * @author graemerocher
 * @since 1.0
 */
public class AsciiDocPropertyReferenceWriter implements ConfigurationMetadataWriter {

    private static final String AT_PARAM = "@param";
    private static final Pattern PARAM_PATTERN = Pattern.compile(AT_PARAM + "\\s*\\w+\\s*(.+)");

    @Override
    public void write(ConfigurationMetadataBuilder metadataBuilder, ClassWriterOutputVisitor classWriterOutputVisitor) throws IOException {

        List<PropertyMetadata> props = new ArrayList<>(metadataBuilder.getProperties())
                .stream()
                .filter(distinctByKey(PropertyMetadata::getPath)).toList();

        List<ConfigurationMetadata> configs = new ArrayList<>(metadataBuilder.getConfigurations())
                .stream()
                .sorted(Comparator.comparing(ConfigurationMetadata::getName))
                .collect(Collectors.toList());

        configs.removeIf(config -> props.stream()
                .noneMatch(pm -> pm.getDeclaringType().equals(config.getType())));

        if (CollectionUtils.isNotEmpty(configs)) {

            Optional<GeneratedFile> file = classWriterOutputVisitor.visitMetaInfFile("config-properties.adoc", Element.EMPTY_ELEMENT_ARRAY);

            if (file.isPresent()) {
                try (BufferedWriter w = new BufferedWriter(file.get().openWriter())) {
                    for (ConfigurationMetadata cm : configs) {
                        List<PropertyMetadata> properties = props.stream().filter(pm -> pm.getDeclaringType().equals(cm.getType())).toList();
                        if (!properties.isEmpty()) {
                            write(w, cm, properties);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"java:S135", "java:S3776"}) // Ignore complexity and too many breaks/continues
    private void write(BufferedWriter w, ConfigurationMetadata cm, @NonNull List<PropertyMetadata> value) throws IOException {
        writeFragmentLink(w, cm.getType());
        w.newLine();
        w.append(".Configuration Properties for api:").append(cm.getType()).append("[]");
        w.newLine();
        w.append("|===");
        w.newLine();
        w.append("|Property |Type |Description |Default value");
        w.newLine();

        for (PropertyMetadata pm : value) {
            //ignore setters of configuration properties classes
            final String pmType = pm.getType();
            if (pmType == null || pmType.equals(cm.getType())) {
                continue;
            }

            String path = pm.getPath();
            String description = pm.getDescription();

            if (path.contains("..")) {
                continue;
            }
            if (StringUtils.isEmpty(description)) {
                description = "";
            }

            description = description.trim();

            if (description.startsWith(AT_PARAM)) {
                Matcher match = PARAM_PATTERN.matcher(description);
                if (match.find()) {
                    description = match.group(1);
                }
            } else if (description.contains(AT_PARAM)) {
                description = description.substring(0, description.indexOf(AT_PARAM)).trim();
            }

            String type = pm.getType();

            if (type.startsWith("io.micronaut")) {
                type = "api:" + type + "[]";
            }

            w.newLine();
            w.append("| `+").append(path).append("+`");
            w.newLine();
            w.append("|").append(type);
            w.newLine();
            w.append("|").append(description);
            w.newLine();
            w.append("|");
            if (pm.getDefaultValue() != null) {
                w.append(pm.getDefaultValue());
            }
            w.newLine();
            w.newLine();
        }

        w.newLine();
        w.append("|===");
        w.newLine();
        w.append("<<<");
    }

    private void writeFragmentLink(BufferedWriter w, String type) throws IOException {
        w.newLine();
        w.append("++++");
        w.newLine();
        w.append("<a id=\"");
        w.append(type);
        w.append("\" href=\"#");
        w.append(type);
        w.append("\">&#128279;</a>");
        w.newLine();
        w.append("++++");
    }

    private <T> Predicate<T> distinctByKey(
            Function<? super T, ?> ke) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(ke.apply(t), Boolean.TRUE) == null;
    }
}
