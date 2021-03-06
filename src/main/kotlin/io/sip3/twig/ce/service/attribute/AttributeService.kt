/*
 * Copyright 2018-2020 SIP3.IO, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sip3.twig.ce.service.attribute

import io.sip3.commons.domain.Attribute
import io.sip3.twig.ce.mongo.MongoClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
open class AttributeService(private val mongoClient: MongoClient) {

    @Cacheable(value = ["listAttributes"])
    open fun list(): Collection<Attribute> {
        val attributes = mutableMapOf<String, Attribute>()

        val collections = mongoClient.listCollectionNames("attributes")
        mongoClient.find(collections).forEach { document ->
            val name = document.getString("name")
            val type = document.getString("type")

            val attribute = attributes.getOrPut(name) {
                Attribute().apply {
                    this.name = name
                    this.type = type
                }
            }

            if (type == Attribute.TYPE_STRING) {
                document.get("options")?.let { options ->
                    if (attribute.options == null) {
                        attribute.options = mutableSetOf()
                    }
                    
                    @Suppress("UNCHECKED_CAST")
                    attribute.options!!.addAll(options as List<String>)
                }
            }
        }

        return attributes.values
    }
}