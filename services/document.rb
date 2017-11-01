require "kafka"

def add_document(id, payload)
  statement = $session.prepare('INSERT INTO xadf.invoices JSON ?')
  data = { id: id }.merge payload
  $session.execute(statement, arguments: [json(data)])

  deliver_document id
  return {id: id}
end

def deliver_document(id)
  topic = 'xadf.compute.documents'
  producer = $kafka.async_producer
  producer.produce(id, topic: topic)
  producer.deliver_messages
  producer.shutdown
end