require_relative './documents'
require_relative './messages'

module Services
  class Actions
    def initialize(documents, messages, tables)
      @documents = documents
      @messages = messages
      @tables = tables
    end

    def execute(name, payload)
      puts "# (services/actions) execute (name=#{name})"
      @fns ||= {
        'document-add' => method(:document_add),
      }

      fn = @fns.fetch(name, nil)

      if fn
        id = fn.call(payload)
        { status: :ok, id: id }
      else
        { status: :failed, reason: 'unknown_action', message: "Invalid action: #{name}" }
      end
    end

    private

    def document_add(payload)
      puts "# (services/schedule) adding document"
      id = @documents.store(payload)
      puts "# (services/schedule) storing envelope (id=#{id})"
      @tables.store_envelope(id, payload.fetch('envelope', {}))
      puts "# (services/schedule) enque (id=#{id})"
      @messages.deliver_document(id)
      id
    end
  end
end
