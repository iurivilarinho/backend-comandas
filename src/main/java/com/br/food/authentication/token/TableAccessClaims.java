package com.br.food.authentication.token;

/**
 * What a valid table QR token says: which table and which version of that table's QR it was printed from.
 *
 * @param tableNumber number of the table
 * @param version     value of {@code DiningTable.accessVersion} when the token was generated
 */
public record TableAccessClaims(String tableNumber, int version) {
}
