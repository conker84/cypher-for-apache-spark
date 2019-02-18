/*
 * Copyright (c) 2016-2019 "Neo4j Sweden, AB" [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.spark.impl

import org.apache.spark.sql.{Row, functions}
import org.opencypher.okapi.api.io.conversion.{EntityMapping, NodeMapping, RelationshipMapping}
import org.opencypher.okapi.api.types.{CTNode, CTRelationship}
import org.opencypher.okapi.api.value.CypherValue.CypherMap
import org.opencypher.okapi.testing.Bag
import org.opencypher.okapi.testing.propertygraph.CreateGraphFactory
import org.opencypher.spark.api.io.{CAPSNodeTable, CAPSRelationshipTable}
import org.opencypher.spark.api.value.CAPSEntity._
import org.opencypher.spark.api.io.CAPSEntityTable
import org.opencypher.spark.api.value.CAPSRelationship
import org.opencypher.spark.impl.CAPSConverters._
import org.opencypher.spark.testing.support.creation.caps.{CAPSScanGraphFactory, CAPSTestGraphFactory}

class ScanGraphTest extends CAPSGraphTest {

  override def capsGraphFactory: CAPSTestGraphFactory = CAPSScanGraphFactory

  it("executes union") {
    val graph1 = caps.graphs.create(personTable, knowsTable)
    val graph2 = caps.graphs.create(programmerTable, bookTable, readsTable)

    val result = graph1 unionAll graph2

    val nodeCols = Seq(
      n,
      nHasLabelBook,
      nHasLabelPerson,
      nHasLabelProgrammer,
      nHasLabelSwedish,
      nHasPropertyLanguage,
      nHasPropertyLuckyNumber,
      nHasPropertyName,
      nHasPropertyTitle,
      nHasPropertyYear
    )
    val relCols = Seq(
      rStart,
      r,
      rHasTypeKnows,
      rHasTypeReads,
      rEnd,
      rHasPropertyRecommends,
      rHasPropertySince
    )

    val nodeData = Bag(
      Row(1L.encodeAsCAPSId.withPrefix(0).toList, false, true, false, true, null, 23L, "Mats", null, null),
      Row(2L.encodeAsCAPSId.withPrefix(0).toList, false, true, false, false, null, 42L, "Martin", null, null),
      Row(3L.encodeAsCAPSId.withPrefix(0).toList, false, true, false, false, null, 1337L, "Max", null, null),
      Row(4L.encodeAsCAPSId.withPrefix(0).toList, false, true, false, false, null, 9L, "Stefan", null, null),
      Row(10L.encodeAsCAPSId.withPrefix(1).toList, true, false, false, false, null, null, null, "1984", 1949L),
      Row(20L.encodeAsCAPSId.withPrefix(1).toList, true, false, false, false, null, null, null, "Cryptonomicon", 1999L),
      Row(30L.encodeAsCAPSId.withPrefix(1).toList, true, false, false, false, null, null, null, "The Eye of the World", 1990L),
      Row(40L.encodeAsCAPSId.withPrefix(1).toList, true, false, false, false, null, null, null, "The Circle", 2013L),
      Row(100L.encodeAsCAPSId.withPrefix(1).toList, false, true, true, false, "C", 42L, "Alice", null, null),
      Row(200L.encodeAsCAPSId.withPrefix(1).toList, false, true, true, false, "D", 23L, "Bob", null, null),
      Row(300L.encodeAsCAPSId.withPrefix(1).toList, false, true, true, false, "F", 84L, "Eve", null, null),
      Row(400L.encodeAsCAPSId.withPrefix(1).toList, false, true, true, false, "R", 49L, "Carl", null, null)
    )

    val relData = Bag(
      Row(1L.encodeAsCAPSId.withPrefix(0).toList, 1L.encodeAsCAPSId.withPrefix(0).toList, true, false, 2L.encodeAsCAPSId.withPrefix(0).toList, null, 2017L),
      Row(1L.encodeAsCAPSId.withPrefix(0).toList, 2L.encodeAsCAPSId.withPrefix(0).toList, true, false, 3L.encodeAsCAPSId.withPrefix(0).toList, null, 2016L),
      Row(1L.encodeAsCAPSId.withPrefix(0).toList, 3L.encodeAsCAPSId.withPrefix(0).toList, true, false, 4L.encodeAsCAPSId.withPrefix(0).toList, null, 2015L),
      Row(2L.encodeAsCAPSId.withPrefix(0).toList, 4L.encodeAsCAPSId.withPrefix(0).toList, true, false, 3L.encodeAsCAPSId.withPrefix(0).toList, null, 2016L),
      Row(2L.encodeAsCAPSId.withPrefix(0).toList, 5L.encodeAsCAPSId.withPrefix(0).toList, true, false, 4L.encodeAsCAPSId.withPrefix(0).toList, null, 2013L),
      Row(3L.encodeAsCAPSId.withPrefix(0).toList, 6L.encodeAsCAPSId.withPrefix(0).toList, true, false, 4L.encodeAsCAPSId.withPrefix(0).toList, null, 2016L),
      Row(100L.encodeAsCAPSId.withPrefix(1).toList, 100L.encodeAsCAPSId.withPrefix(1).toList, false, true, 10L.encodeAsCAPSId.withPrefix(1).toList, true, null),
      Row(200L.encodeAsCAPSId.withPrefix(1).toList, 200L.encodeAsCAPSId.withPrefix(1).toList, false, true, 40L.encodeAsCAPSId.withPrefix(1).toList, true, null),
      Row(300L.encodeAsCAPSId.withPrefix(1).toList, 300L.encodeAsCAPSId.withPrefix(1).toList, false, true, 30L.encodeAsCAPSId.withPrefix(1).toList, true, null),
      Row(400L.encodeAsCAPSId.withPrefix(1).toList, 400L.encodeAsCAPSId.withPrefix(1).toList, false, true, 20L.encodeAsCAPSId.withPrefix(1).toList, false, null)
    )

    verify(result.nodes("n"), nodeCols, nodeData)
    verify(result.relationships("r"), relCols, relData)
  }

  it("dont lose schema information when mapping") {
    val nodes = CAPSEntityTable.create(NodeMapping.on("id").build,
      caps.sparkSession.createDataFrame(
        Seq(
          Tuple1(10L),
          Tuple1(11L),
          Tuple1(12L),
          Tuple1(20L),
          Tuple1(21L),
          Tuple1(22L),
          Tuple1(25L),
          Tuple1(50L),
          Tuple1(51L)
        )
      ).toDF("id"))

    val rs = CAPSEntityTable.create(RelationshipMapping.on("ID").from("SRC").to("DST").relType("FOO").build,
      caps.sparkSession.createDataFrame(
        Seq(
          (10L, 1000L, 20L),
          (50L, 500L, 25L)
        )
      ).toDF("SRC", "ID", "DST"))


    val graph = caps.graphs.create(nodes, rs)

    val results = graph.relationships("r").asCaps.toCypherMaps

    results.collect().toSet should equal(
      Set(
        CypherMap("r" -> CAPSRelationship(1000L, 10L, 20L, "FOO")),
        CypherMap("r" -> CAPSRelationship(500L, 50L, 25L, "FOO"))
      ))
  }

  it("Construct graph from single node scan") {
    val graph = caps.graphs.create(personTable)
    val nodes = graph.nodes("n")
    val cols = Seq(
      n,
      nHasLabelPerson,
      nHasLabelSwedish,
      nHasPropertyLuckyNumber,
      nHasPropertyName
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, true, true, 23L, "Mats"),
      Row(2L.encodeAsCAPSId.toList, true, false, 42L, "Martin"),
      Row(3L.encodeAsCAPSId.toList, true, false, 1337L, "Max"),
      Row(4L.encodeAsCAPSId.toList, true, false, 9L, "Stefan")
    )
    verify(nodes, cols, data)
  }

  it("Construct graph from multiple node scans") {
    val graph = caps.graphs.create(personTable, bookTable)
    val nodes = graph.nodes("n")
    val cols = Seq(
      n,
      nHasLabelBook,
      nHasLabelPerson,
      nHasLabelSwedish,
      nHasPropertyLuckyNumber,
      nHasPropertyName,
      nHasPropertyTitle,
      nHasPropertyYear
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, false, true, true, 23L, "Mats", null, null),
      Row(2L.encodeAsCAPSId.toList, false, true, false, 42L, "Martin", null, null),
      Row(3L.encodeAsCAPSId.toList, false, true, false, 1337L, "Max", null, null),
      Row(4L.encodeAsCAPSId.toList, false, true, false, 9L, "Stefan", null, null),
      Row(10L.encodeAsCAPSId.toList, true, false, false, null, null, "1984", 1949L),
      Row(20L.encodeAsCAPSId.toList, true, false, false, null, null, "Cryptonomicon", 1999L),
      Row(30L.encodeAsCAPSId.toList, true, false, false, null, null, "The Eye of the World", 1990L),
      Row(40L.encodeAsCAPSId.toList, true, false, false, null, null, "The Circle", 2013L)
    )
    verify(nodes, cols, data)
  }


  it("Align node scans") {
    val fixture =
      """
        |CREATE (a:A { name: 'A' })
        |CREATE (b:B { name: 'B' })
        |CREATE (combo:A:B { name: 'COMBO', size: 2 })
        |CREATE (a)-[:R { since: 2004 }]->(b)
        |CREATE (b)-[:R { since: 2005 }]->(combo)
        |CREATE (combo)-[:S { since: 2006 }]->(combo)
      """.stripMargin

    val graph = CAPSScanGraphFactory(CreateGraphFactory(fixture))

    graph.cypher("MATCH (n) RETURN n").records.size should equal(3)
  }

  it("Align node scans when individual tables have the same node id and properties") {
    val aDf = caps.sparkSession.createDataFrame(Seq(
      (0L, "A")
    )).toDF("_node_id", "name").withColumn("size", functions.lit(null))
    val aMapping: EntityMapping = NodeMapping.on("_node_id").withPropertyKey("name").withPropertyKey("size").withImpliedLabel("A").build
    val aTable = CAPSEntityTable.create(aMapping, aDf)

    val bDf = caps.sparkSession.createDataFrame(Seq(
      (1L, "B")
    )).toDF("_node_id", "name").withColumn("size", functions.lit(null))
    val bMapping = NodeMapping.on("_node_id").withPropertyKey("name").withPropertyKey("size").withImpliedLabel("B").build
    val bTable = CAPSEntityTable.create(bMapping, bDf)

    val comboDf = caps.sparkSession.createDataFrame(Seq(
      (2L, "COMBO", 2)
    )).toDF("_node_id", "name", "size")
    val comboMapping = NodeMapping.on("_node_id").withPropertyKey("name").withPropertyKey("size").withImpliedLabels("A", "B").build
    val comboTable = CAPSEntityTable.create(comboMapping, comboDf)

    val graph = caps.graphs.create(aTable, bTable, comboTable)

    graph.cypher("MATCH (n) RETURN n").records.size should equal(3)
  }

  it("Construct graph from single node and single relationship scan") {
    val graph = caps.graphs.create(personTable, knowsTable)
    val rels = graph.relationships("r")

    val cols = Seq(
      rStart,
      r,
      rHasTypeKnows,
      rEnd,
      rHasPropertySince
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, 1L.encodeAsCAPSId.toList, true, 2L.encodeAsCAPSId.toList, 2017L),
      Row(1L.encodeAsCAPSId.toList, 2L.encodeAsCAPSId.toList, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(1L.encodeAsCAPSId.toList, 3L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2015L),
      Row(2L.encodeAsCAPSId.toList, 4L.encodeAsCAPSId.toList, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(2L.encodeAsCAPSId.toList, 5L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2013L),
      Row(3L.encodeAsCAPSId.toList, 6L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2016L)
    )

    verify(rels, cols, data)
  }

  it("Extract all node scans") {
    val graph = caps.graphs.create(personTable, bookTable)
    val nodes = graph.nodes("n", CTNode())
    val cols = Seq(
      n,
      nHasLabelBook,
      nHasLabelPerson,
      nHasLabelSwedish,
      nHasPropertyLuckyNumber,
      nHasPropertyName,
      nHasPropertyTitle,
      nHasPropertyYear
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, false, true, true, 23L, "Mats", null, null),
      Row(2L.encodeAsCAPSId.toList, false, true, false, 42L, "Martin", null, null),
      Row(3L.encodeAsCAPSId.toList, false, true, false, 1337L, "Max", null, null),
      Row(4L.encodeAsCAPSId.toList, false, true, false, 9L, "Stefan", null, null),
      Row(10L.encodeAsCAPSId.toList, true, false, false, null, null, "1984", 1949L),
      Row(20L.encodeAsCAPSId.toList, true, false, false, null, null, "Cryptonomicon", 1999L),
      Row(30L.encodeAsCAPSId.toList, true, false, false, null, null, "The Eye of the World", 1990L),
      Row(40L.encodeAsCAPSId.toList, true, false, false, null, null, "The Circle", 2013L)
    )

    verify(nodes, cols, data)
  }

  it("Extract node scan subset") {
    val graph = caps.graphs.create(personTable, bookTable)
    val nodes = graph.nodes("n", CTNode("Person"))
    val cols = Seq(
      n,
      nHasLabelPerson,
      nHasLabelSwedish,
      nHasPropertyLuckyNumber,
      nHasPropertyName
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, true, true, 23L, "Mats"),
      Row(2L.encodeAsCAPSId.toList, true, false, 42L, "Martin"),
      Row(3L.encodeAsCAPSId.toList, true, false, 1337L, "Max"),
      Row(4L.encodeAsCAPSId.toList, true, false, 9L, "Stefan")
    )
    verify(nodes, cols, data)
  }

  it("Extract all relationship scans") {
    val graph = caps.graphs.create(personTable, bookTable, knowsTable, readsTable)
    val rels = graph.relationships("r")
    val cols = Seq(
      rStart,
      r,
      rHasTypeKnows,
      rHasTypeReads,
      rEnd,
      rHasPropertyRecommends,
      rHasPropertySince
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, 1L.encodeAsCAPSId.toList, true, false, 2L.encodeAsCAPSId.toList, null, 2017L),
      Row(1L.encodeAsCAPSId.toList, 2L.encodeAsCAPSId.toList, true, false, 3L.encodeAsCAPSId.toList, null, 2016L),
      Row(1L.encodeAsCAPSId.toList, 3L.encodeAsCAPSId.toList, true, false, 4L.encodeAsCAPSId.toList, null, 2015L),
      Row(2L.encodeAsCAPSId.toList, 4L.encodeAsCAPSId.toList, true, false, 3L.encodeAsCAPSId.toList, null, 2016L),
      Row(2L.encodeAsCAPSId.toList, 5L.encodeAsCAPSId.toList, true, false, 4L.encodeAsCAPSId.toList, null, 2013L),
      Row(3L.encodeAsCAPSId.toList, 6L.encodeAsCAPSId.toList, true, false, 4L.encodeAsCAPSId.toList, null, 2016L),
      Row(100L.encodeAsCAPSId.toList, 100L.encodeAsCAPSId.toList, false, true, 10L.encodeAsCAPSId.toList, true, null),
      Row(200L.encodeAsCAPSId.toList, 200L.encodeAsCAPSId.toList, false, true, 40L.encodeAsCAPSId.toList, true, null),
      Row(300L.encodeAsCAPSId.toList, 300L.encodeAsCAPSId.toList, false, true, 30L.encodeAsCAPSId.toList, true, null),
      Row(400L.encodeAsCAPSId.toList, 400L.encodeAsCAPSId.toList, false, true, 20L.encodeAsCAPSId.toList, false, null)
    )

    verify(rels, cols, data)
  }

  it("Extract relationship scan subset") {
    val graph = caps.graphs.create(personTable, bookTable, knowsTable, readsTable)
    val rels = graph.relationships("r", CTRelationship("KNOWS"))
    val cols = Seq(
      rStart,
      r,
      rHasTypeKnows,
      rEnd,
      rHasPropertySince
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, 1L.encodeAsCAPSId.toList, true, 2L.encodeAsCAPSId.toList, 2017L),
      Row(1L.encodeAsCAPSId.toList, 2L.encodeAsCAPSId.toList, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(1L.encodeAsCAPSId.toList, 3L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2015L),
      Row(2L.encodeAsCAPSId.toList, 4L.encodeAsCAPSId.toList, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(2L.encodeAsCAPSId.toList, 5L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2013L),
      Row(3L.encodeAsCAPSId.toList, 6L.encodeAsCAPSId.toList, true, 4L.encodeAsCAPSId.toList, 2016L)
    )

    verify(rels, cols, data)
  }

  it("Extract relationship scan strict subset") {
    val graph = caps.graphs.create(personTable, bookTable, knowsTable, readsTable, influencesTable)
    val rels = graph.relationships("r", CTRelationship("KNOWS", "INFLUENCES"))
    val cols = Seq(
      rStart,
      r,
      rHasTypeInfluences,
      rHasTypeKnows,
      rEnd,
      rHasPropertySince
    )
    val data = Bag(
      // :KNOWS
      Row(1L.encodeAsCAPSId.toList, 1L.encodeAsCAPSId.toList, false, true, 2L.encodeAsCAPSId.toList, 2017L),
      Row(1L.encodeAsCAPSId.toList, 2L.encodeAsCAPSId.toList, false, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(1L.encodeAsCAPSId.toList, 3L.encodeAsCAPSId.toList, false, true, 4L.encodeAsCAPSId.toList, 2015L),
      Row(2L.encodeAsCAPSId.toList, 4L.encodeAsCAPSId.toList, false, true, 3L.encodeAsCAPSId.toList, 2016L),
      Row(2L.encodeAsCAPSId.toList, 5L.encodeAsCAPSId.toList, false, true, 4L.encodeAsCAPSId.toList, 2013L),
      Row(3L.encodeAsCAPSId.toList, 6L.encodeAsCAPSId.toList, false, true, 4L.encodeAsCAPSId.toList, 2016L),
      // :INFLUENCES
      Row(10L.encodeAsCAPSId.toList, 1000L.encodeAsCAPSId.toList, true, false, 20L.encodeAsCAPSId.toList, null)
    )

    verify(rels, cols, data)
  }

  it("Extract from scans with overlapping labels") {
    val graph = caps.graphs.create(personTable, programmerTable)
    val nodes = graph.nodes("n", CTNode("Person"))
    val cols = Seq(
      n,
      nHasLabelPerson,
      nHasLabelProgrammer,
      nHasLabelSwedish,
      nHasPropertyLanguage,
      nHasPropertyLuckyNumber,
      nHasPropertyName
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, true, false, true, null, 23L, "Mats"),
      Row(2L.encodeAsCAPSId.toList, true, false, false, null, 42L, "Martin"),
      Row(3L.encodeAsCAPSId.toList, true, false, false, null, 1337L, "Max"),
      Row(4L.encodeAsCAPSId.toList, true, false, false, null, 9L, "Stefan"),
      Row(100L.encodeAsCAPSId.toList, true, true, false, "C", 42L, "Alice"),
      Row(200L.encodeAsCAPSId.toList, true, true, false, "D", 23L, "Bob"),
      Row(300L.encodeAsCAPSId.toList, true, true, false, "F", 84L, "Eve"),
      Row(400L.encodeAsCAPSId.toList, true, true, false, "R", 49L, "Carl")
    )

    verify(nodes, cols, data)
  }

  it("Extract from scans with implied label but missing keys") {
    val graph = caps.graphs.create(personTable, brogrammerTable)
    val nodes = graph.nodes("n", CTNode("Person"))
    val cols = Seq(
      n,
      nHasLabelBrogrammer,
      nHasLabelPerson,
      nHasLabelSwedish,
      nHasPropertyLanguage,
      nHasPropertyLuckyNumber,
      nHasPropertyName
    )
    val data = Bag(
      Row(1L.encodeAsCAPSId.toList, false, true, true, null, 23L, "Mats"),
      Row(2L.encodeAsCAPSId.toList, false, true, false, null, 42L, "Martin"),
      Row(3L.encodeAsCAPSId.toList, false, true, false, null, 1337L, "Max"),
      Row(4L.encodeAsCAPSId.toList, false, true, false, null, 9L, "Stefan"),
      Row(100L.encodeAsCAPSId.toList, true, true, false, "Node", null, null),
      Row(200L.encodeAsCAPSId.toList, true, true, false, "Coffeescript", null, null),
      Row(300L.encodeAsCAPSId.toList, true, true, false, "Javascript", null, null),
      Row(400L.encodeAsCAPSId.toList, true, true, false, "Typescript", null, null)
    )

    verify(nodes, cols, data)
  }
}
