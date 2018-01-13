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
      @fns ||= {
        'document-add' => method(:document_add),
      }

      fn = @fns.fetch(name, nil)

      if fn
        fn.call(payload)
        { status: :ok }
      else
        { status: :failed, reason: 'unknown_action', message: "Invalid action: #{name}" }
      end
    end

    private
    
    def document_add(payload)
      id = @documents.store(payload)
      @tables.store_envelope(id, payload.fetch('envelope', {}))
      @messages.deliver_document(id)
    end
  end
end
