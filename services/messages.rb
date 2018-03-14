require 'kafka'

module Services
  class Messages
    TOPIC = 'xadf.compute.documents'

    def initialize(opts)
      puts "> connecting to kafka (hosts=#{opts['broker_hosts']})"
      @kafka = Kafka.new(
        seed_brokers: opts['broker_hosts'],
        # Set an optional client id in order to identify the client to Kafka:
        client_id: opts['client'],
      )
      puts "< connected"
    end
    
    def deliver_document(id)
      with_producer do |producer|
        producer.produce(id, topic: TOPIC)
      end
    end

    private

    def with_producer
      producer = @kafka.async_producer
      yield(producer)
      producer.deliver_messages
      producer.shutdown
    end
  end
end
