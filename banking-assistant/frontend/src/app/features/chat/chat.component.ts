import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { ChatService } from '../../core/services/chat.service';
import { ChatBubble } from '../../core/models/chat.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html'
})
export class ChatComponent implements AfterViewChecked {
  @ViewChild('scrollAnchor') scrollAnchor!: ElementRef<HTMLDivElement>;

  messages: ChatBubble[] = [
    { sender: 'ASSISTANT', content: "Hi, I'm your banking assistant. Ask me about your accounts, loans, bank policies, or get a personalised financial plan." }
  ];
  draft = '';
  sessionId: string | undefined;
  sending = false;
  private shouldScroll = false;

  constructor(private chatService: ChatService) {}

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.scrollAnchor) {
      this.scrollAnchor.nativeElement.scrollIntoView({ behavior: 'smooth' });
      this.shouldScroll = false;
    }
  }

  send(): void {
    const text = this.draft.trim();
    if (!text || this.sending) return;

    this.messages.push({ sender: 'USER', content: text });
    this.messages.push({ sender: 'ASSISTANT', content: '', pending: true });
    this.draft = '';
    this.sending = true;
    this.shouldScroll = true;

    this.chatService.sendMessage({ message: text, sessionId: this.sessionId }).subscribe({
      next: (res) => {
        this.sessionId = res.sessionId;
        this.messages[this.messages.length - 1] = {
          sender: 'ASSISTANT',
          content: res.reply,
          blocked: res.blocked
        };
        this.sending = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.messages[this.messages.length - 1] = {
          sender: 'ASSISTANT',
          content: "Sorry, something went wrong reaching the assistant. Please try again.",
          blocked: true
        };
        this.sending = false;
        this.shouldScroll = true;
      }
    });
  }

  suggestedPrompts = [
    'What is my account balance?',
    'Can I get a personal loan of 2 lakh rupees?',
    'What are the KYC requirements?',
    'Help me plan savings for a house down payment'
  ];

  useSuggested(prompt: string): void {
    this.draft = prompt;
    this.send();
  }
}
