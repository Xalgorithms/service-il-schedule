def add_document(id, payload)
  statement = $session.prepare('INSERT INTO xadf.invoices JSON ?')
  data = { id: id }.merge payload
  $session.execute(statement, arguments: [json(data)])

  return {id: id}
end