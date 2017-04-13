package org.opencypher.spark.impl.logical

import org.opencypher.spark.api.types._
import org.opencypher.spark.api.expr._
import org.opencypher.spark.api.ir.pattern.{EveryNode, EveryRelationship}
import org.opencypher.spark.api.ir.{Field, SolvedQueryModel}
import org.opencypher.spark.api.record.{ProjectedExpr, ProjectedField, RecordHeader}
import org.opencypher.spark.impl.util._

class LogicalOperatorProducer {

  def planTargetExpand(source: Field, rel: Field, target: Field, prev: LogicalOperator): ExpandTarget = {
    val signature = RecordHeader.empty
    val solved = prev.solved.withFields(rel, source)

    ExpandTarget(source, rel, target, prev, signature)(solved)
  }

  def planSourceExpand(source: Field, r: (Field, EveryRelationship), target: Field, prev: LogicalOperator): ExpandSource = {
    val signature = RecordHeader.empty

    val (rel, types) = r

    val solved = types.relTypes.elts.foldLeft(prev.solved.withFields(rel, target)) {
      case (acc, next) => acc.withPredicate(HasType(rel, next)(CTBoolean))
    }

    ExpandSource(source, rel, types, target, prev, signature)(solved)
  }

  def planNodeScan(node: Field, everyNode: EveryNode): NodeScan = {
    val signature = RecordHeader.empty

    val solved = everyNode.labels.elts.foldLeft(SolvedQueryModel.empty[Expr].withField(node)) {
      case (acc, ref) => acc.withPredicate(HasLabel(node, ref)(CTBoolean))
    }

    NodeScan(node, everyNode, signature)(solved)
  }

  def planFilter(expr: Expr, prev: LogicalOperator): Filter = {
    val signature = RecordHeader.empty

    Filter(expr, prev, signature)(prev.solved.withPredicate(expr))
  }

  def projectField(field: Field, expr: Expr, prev: LogicalOperator): Project = {
    val signature = RecordHeader.empty
    val projection = ProjectedField(field, expr)

    Project(projection, prev, signature)(prev.solved.withField(field))
  }

  def projectExpr(expr: Expr, prev: LogicalOperator): Project = {
    val signature = RecordHeader.empty
    val projection = ProjectedExpr(expr)

    Project(projection, prev, signature)(prev.solved)
  }

  def planSelect(fields: Set[Var], prev: LogicalOperator): Select = {
    val signature = RecordHeader.empty

    Select(fields, prev, signature)(prev.solved)
  }
}
