package services

import javax.inject.Inject
import models.messages.InterchangeControlReference
import repositories.InterchangeControlReferenceIdRepository

import scala.concurrent.{ExecutionContext, Future}

class DatabaseService @Inject()(interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository) {

  def getInterchangeControlReferenceId()(implicit ec: ExecutionContext): Future[Either[FailedCreatingInterchangeControlReference, InterchangeControlReference]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .map {
        reference =>
          Right(reference)
      }
      .recover {
        case _ =>
          Left(FailedCreatingInterchangeControlReference)
      }
}

sealed trait FailedCreatingInterchangeControlReference
object FailedCreatingInterchangeControlReference extends FailedCreatingInterchangeControlReference

