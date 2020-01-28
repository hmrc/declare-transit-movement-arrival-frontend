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

package forms.mappings

trait FormEqualityCheck[A] {
  def equalsString(lhs: A, formValue: String): Boolean
}

object FormEqualityCheck {

  def apply[A](checker: (A, String) => Boolean): FormEqualityCheck[A] = new FormEqualityCheck[A] {
    override def equalsString(lhs: A, formValue: String): Boolean = checker(lhs, formValue)
  }

  implicit class FormEqualityCheckOps[A: FormEqualityCheck](lhs: A) {
    def equalsString(formValue: String): Boolean = implicitly[FormEqualityCheck[A]].equalsString(lhs, formValue)
  }

  implicit val stringFormEquality: FormEqualityCheck[String] = FormEqualityCheck[String](_ == _)
  implicit val integerFormEquality: FormEqualityCheck[Int]   = FormEqualityCheck[Int](_.toString == _)
}
