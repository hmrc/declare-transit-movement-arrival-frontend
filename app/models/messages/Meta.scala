/*
 * Copyright 2020 HM Revenue & Customs
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
 */

package models.messages

import java.time.{LocalDate, LocalTime}

import models.XMLWrites
import models.XMLWrites._
import utils.Format

import scala.xml.NodeSeq

case class Meta(messageSender: MessageSender,
                interchangeControlReference: InterchangeControlReference,
                dateOfPreparation: LocalDate,
                timeOfPreparation: LocalTime,
                senderIdentificationCodeQualifier: Option[String]    = None,
                recipientIdentificationCodeQualifier: Option[String] = None,
                recipientsReferencePassword: Option[String]          = None,
                recipientsReferencePasswordQualifier: Option[String] = None,
                priority: Option[String]                             = None,
                acknowledgementRequest: Option[String]               = None,
                communicationsAgreementId: Option[String]            = None,
                commonAccessReference: Option[String]                = None,
                messageSequenceNumber: Option[String]                = None,
                firstAndLastTransfer: Option[String]                 = None)

object Meta {

  implicit def writes: XMLWrites[Meta] =
    XMLWrites(
      a =>
        <SynIdeMES1>UNOC</SynIdeMES1>
        <SynVerNumMES2>3</SynVerNumMES2> ++ {
          a.messageSender.toXml
        } ++ {
          a.senderIdentificationCodeQualifier.fold(NodeSeq.Empty) {
            senderIdentificationCodeQualifier =>
              <SenIdeCodQuaMES4>{escapeXml(senderIdentificationCodeQualifier)}</SenIdeCodQuaMES4>
          }
        } ++ {
          a.recipientIdentificationCodeQualifier.fold(NodeSeq.Empty) {
            recipientIdentificationCodeQualifier =>
              <RecIdeCodQuaMES7>{escapeXml(recipientIdentificationCodeQualifier)}</RecIdeCodQuaMES7>
          }
        } ++
          <MesRecMES6>NCTS</MesRecMES6>
        <DatOfPreMES9>{Format.dateFormatted(a.dateOfPreparation)}</DatOfPreMES9>
        <TimOfPreMES10>{Format.timeFormatted(a.timeOfPreparation)}</TimOfPreMES10> ++ {
          a.interchangeControlReference.toXml
        } ++ {
          a.recipientsReferencePassword.fold(NodeSeq.Empty) {
            recipientsReferencePassword =>
              <RecRefMES12>{escapeXml(recipientsReferencePassword)}</RecRefMES12>
          }
        } ++ {
          a.recipientsReferencePasswordQualifier.fold(NodeSeq.Empty) {
            recipientsReferencePasswordQualifier =>
              <RecRefQuaMES13>{escapeXml(recipientsReferencePasswordQualifier)}</RecRefQuaMES13>
          }
        } ++
          <AppRefMES14>NCTS</AppRefMES14> ++ {

          a.priority.fold(NodeSeq.Empty) {
            priority =>
              <PriMES15>{escapeXml(priority)}</PriMES15>
          }
        } ++ {

          a.acknowledgementRequest.fold(NodeSeq.Empty) {
            acknowledgementRequest =>
              <AckReqMES16>{acknowledgementRequest}</AckReqMES16>
          }
        } ++ {

          a.communicationsAgreementId.fold(NodeSeq.Empty) {
            communicationsAgreementId =>
              <ComAgrIdMES17>{escapeXml(communicationsAgreementId)}</ComAgrIdMES17>
          }

        } ++
          <TesIndMES18>0</TesIndMES18>
        <MesIdeMES19>1</MesIdeMES19>
        <MesTypMES20>GB007A</MesTypMES20> ++ {

          a.commonAccessReference.fold(NodeSeq.Empty) {
            commonAccessReference =>
              <ComAccRefMES21>{escapeXml(commonAccessReference)}</ComAccRefMES21>
          }
        } ++ {

          a.messageSequenceNumber.fold(NodeSeq.Empty) {
            messageSequenceNumber =>
              <MesSeqNumMES22>{escapeXml(messageSequenceNumber)}</MesSeqNumMES22>
          }
        } ++ {

          a.firstAndLastTransfer.fold(NodeSeq.Empty) {
            firstAndLastTransfer =>
              <FirAndLasTraMES23>{escapeXml(firstAndLastTransfer)}</FirAndLasTraMES23>
          }
      })

}
